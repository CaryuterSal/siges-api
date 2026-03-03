package dev.spiffocode.sigesapi.users.infrastructure.service.impl;

import dev.spiffocode.sigesapi.common.infrastructure.persistence.WithDeletedRecords;
import dev.spiffocode.sigesapi.users.domain.exception.EmailExistsException;
import dev.spiffocode.sigesapi.users.domain.exception.PhoneNumberExistsException;
import dev.spiffocode.sigesapi.users.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserUniquenessValidator {

    private final UserRepository userRepository;

    @WithDeletedRecords
    public void assertRegisterUnique(String email, String phoneNumber){
        if(userRepository.existsByEmail(email)){
            throw new EmailExistsException(email);
        }
        if(userRepository.existsByPhoneNumber(phoneNumber)){
            throw new PhoneNumberExistsException(phoneNumber);
        }
    }

    @WithDeletedRecords
    public void assertCommonInfoUpdateUnique(Long entityId, String phoneNumber){
        if(userRepository.existsByPhoneNumberAndIdNot(phoneNumber, entityId)){
            throw new PhoneNumberExistsException(phoneNumber);
        }
    }

    @WithDeletedRecords
    public void assertEmailChangeUnique(Long entityId, String email){
        if(userRepository.existsByEmailAndIdNot(email, entityId)){
            throw new EmailExistsException(email);
        }
    }
}
