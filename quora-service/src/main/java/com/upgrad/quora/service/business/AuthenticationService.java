package com.upgrad.quora.service.business;

import com.upgrad.quora.service.common.ErrorMessage;
import com.upgrad.quora.service.dao.UserDao;
import com.upgrad.quora.service.entity.UserAuthEntity;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.AuthenticationFailedException;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.SignOutRestrictedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.UUID;

@Service
public class AuthenticationService {

    @Autowired
    private UserDao userDao;

    @Transactional
    public UserAuthEntity authenticate(String username, String password) throws AuthenticationFailedException {

        UserEntity userEntity = userDao.getUserByUserName(username);
        if(userEntity == null){
            throw new AuthenticationFailedException("ATH-001", "This username does not exist");
        }

        String encryptedPassword =  PasswordCryptographyProvider.encrypt(password, userEntity.getSalt());
        if(userEntity.getPassword().equals(encryptedPassword)){

            JwtTokenProvider tokenProvider = new JwtTokenProvider(encryptedPassword);
            final ZonedDateTime now = ZonedDateTime.now();
            final ZonedDateTime expiresAt = now.plusHours(8);

            UserAuthEntity userAuthEntity = new UserAuthEntity();
            userAuthEntity.setUser(userEntity);
            userAuthEntity.setUuid(UUID.randomUUID().toString());
            userAuthEntity.setAccessToken(tokenProvider.generateToken(userEntity.getUuid(),now,expiresAt));
            userAuthEntity.setLoginAt(now);
            userAuthEntity.setExpiresAt(expiresAt);

            return userDao.createAuthToken(userAuthEntity);

        }else{
            throw new AuthenticationFailedException("ATH-002","Password failed");
        }
    }

    @Transactional
    public String signOut(String accessToken) throws SignOutRestrictedException {
        UserAuthEntity userAuthEntity = userDao.getUserByAccessToken(accessToken);
        if(userAuthEntity == null){
            throw new SignOutRestrictedException("SGR-001","User is not Signed in");
        }

        userAuthEntity.setLogoutAt(ZonedDateTime.now());
        userDao.updateUserAuth(userAuthEntity);
        return userAuthEntity.getUser().getUuid();

    }

    /**
     * Method validates that access-token is not expired and user is signed in
     * @param authorization  access-token
     * @param errorMessage  customized error message if access-token is expired
     * @return User of corresponding access-token
     * @throws AuthorizationFailedException
     */
    private UserEntity validateToken(String authorization, final ErrorMessage errorMessage) throws AuthorizationFailedException {
        UserAuthEntity userAuthEntity = userDao.getUserByAccessToken(authorization);
        if(userAuthEntity == null){
            throw new AuthorizationFailedException("ATHR-001","User has not signed in");
        }
        else if(userAuthEntity.getLogoutAt() != null){
            throw new AuthorizationFailedException("ATHR-002", errorMessage.toString());
        }

        return userAuthEntity.getUser();
    }

    /** Method validates that access-token is not expired and user is signed in
     * @param authorization  access-token
     * @return User of corresponding access-token
     * @throws AuthorizationFailedException
     */
    public UserEntity validateTokenForGetAllAnswersEndpoint(final String authorization) throws AuthorizationFailedException {
        return this.validateToken(authorization, ErrorMessage.USER_SIGNED_OUT_CAN_NOT_GET_ALL_ANSWER );
    }


    public UserEntity validateTokenForDeleteAnswerEndpoint(final String authorization) throws AuthorizationFailedException {
        return this.validateToken(authorization, ErrorMessage.USER_SIGNED_OUT_CAN_NOT_DELETE_AN_ANSWER);
    }
	public UserEntity validateTokenForCreateAnswerEndpoint(final String authorization) throws AuthorizationFailedException {
        return this.validateToken(authorization, ErrorMessage.USER_SIGNED_OUT_CAN_NOT_CREATEANSWER);
    }
	public UserEntity validateTokenForGetAllquestionsEndpoint(final String authorization) throws AuthorizationFailedException {
        return this.validateToken(authorization, ErrorMessage.USER_SIGNED_OUT_CAN_NOT_GETALLQUESTIONS);
    }
}
