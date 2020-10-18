package com.upgrad.quora.api.controller;

import com.upgrad.quora.api.model.AnswerDeleteResponse;
import com.upgrad.quora.api.model.AnswerDetailsResponse;
import com.upgrad.quora.service.business.AnswerService;
import com.upgrad.quora.service.business.AuthenticationService;
import com.upgrad.quora.service.business.QuestionService;
import com.upgrad.quora.service.entity.AnswerEntity;
import com.upgrad.quora.service.entity.QuestionEntity;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.AnswerNotFoundException;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.InvalidQuestionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/")
public class AnswerController {

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private QuestionService questionService;

    @Autowired
    private AnswerService answerService;
	
	@RequestMapping(
      method = RequestMethod.POST,
      path = "/question/{questionId}/answer/create",
      produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public ResponseEntity<AnswerResponse> createAnswer(
      @RequestHeader("authorization") final String accessToken, AnswerRequest answerRequest)
      throws AuthorizationFailedException {
    AnswerEntity answerEntity = new AnswerEntity();
    answerEntity.setContent(answerRequest.getContent());
    answerEntity = answerService.createAnswer(answerEntity, accessToken);
    AnswerResponse answerResponse = new AnswerResponse();
    answerResponse.setId(answerEntity.getUuid());
    answerResponse.setStatus("Answer Stored");
    return new ResponseEntity<QuestionResponse>(answerResponse, HttpStatus.CREATED);
  }

  /**
   * Get all questions posted by any user.
   *
   * @param accessToken access token to authenticate user.
   * @return List of QuestionDetailsResponse
   * @throws AuthorizationFailedException In case the access token is invalid.
   */
  @RequestMapping(
      method = RequestMethod.GET,
      path = "question/all/{userId}",
      produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public ResponseEntity<List<QuestionDetailsResponse>> getAllQuestions(
      @RequestHeader("authorization") final String accessToken)
      throws AuthorizationFailedException {
    List<QuestionEntity> questions = questionService.getAllQuestions(accessToken);
    List<QuestionDetailsResponse> questionDetailResponses = new ArrayList<>();
    for (QuestionEntity questionEntity : questions) {
      QuestionDetailsResponse questionDetailResponse = new QuestionDetailsResponse();
      questionDetailResponse.setId(questionEntity.getUuid());
      questionDetailResponse.setContent(questionEntity.getContent());
      questionDetailResponses.add(questionDetailResponse);
    }
    return new ResponseEntity<List<QuestionDetailsResponse>>(
        questionDetailResponses, HttpStatus.OK);
  }

  /**
   * Edit a answer
   *

   */
  @RequestMapping(
      method = RequestMethod.PUT,
      path = "/answer/edit/{questionId}",
      produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public ResponseEntity<QuestionEditResponse> editanswer(
      @RequestHeader("authorization") final String accessToken,
      @PathVariable("answerId") final String questionId,
      AnswerEditRequest answerEditRequest)
      throws AuthorizationFailedException, InvalidAnswerException {
    AnswerEntity answerEntity =
        answerService.editAnswer(accessToken, answerId, answerEditRequest.getContent());
    AnswerEditResponse answerEditResponse = new AnswerEditResponse();
    answerEditResponse.setId(answerEntity.getUuid());
    answerEditResponse.setStatus("EDITED ANSWER");
    return new ResponseEntity<AnswerEditResponse>(answerEditResponse, HttpStatus.OK);
  }


    @GetMapping(path = "/answer/all/{questionId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<AnswerDetailsResponse> getAllAnswersToQuestion(@RequestHeader("authorization") final String authorization,
                                                                         @PathVariable("questionId") final String questionUuid) throws AuthorizationFailedException, InvalidQuestionException {

       UserEntity userEntity = authenticationService.validateTokenForGetAllAnswersEndpoint(authorization);

        QuestionEntity questionEntity =  questionService.getQuestionByUuid(questionUuid);

        //TO DO : add answer content after clarification of query on discussion form
        AnswerDetailsResponse answerDetailsResponse = new AnswerDetailsResponse().id(questionEntity.getUuid())
                .questionContent(questionEntity.getContent());

        return new ResponseEntity<AnswerDetailsResponse>(answerDetailsResponse, HttpStatus.OK);


    }

    @DeleteMapping(path = "/answer/delete/{answerId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<AnswerDeleteResponse> delete(@RequestHeader("authorization") final String authorization,
                                                       @PathVariable("answerId") final String answerUuid) throws AuthorizationFailedException, AnswerNotFoundException {

        UserEntity userEntity =  authenticationService.validateTokenForDeleteAnswerEndpoint(authorization);
        AnswerEntity  answerEntity = answerService.getAnswerByUuid(answerUuid);

        answerService.authorize(answerEntity, userEntity, answerUuid);
        answerService.delete(answerEntity);

        AnswerDeleteResponse answerDeleteResponse = new AnswerDeleteResponse().id( answerEntity.getUuid()).status("ANSWER DELETED");
        return new ResponseEntity<AnswerDeleteResponse>( answerDeleteResponse, HttpStatus.ACCEPTED);

    }

}
