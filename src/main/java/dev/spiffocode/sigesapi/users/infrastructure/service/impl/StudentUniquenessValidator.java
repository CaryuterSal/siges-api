package dev.spiffocode.sigesapi.users.infrastructure.service.impl;

import dev.spiffocode.sigesapi.common.infrastructure.persistence.WithDeletedRecords;
import dev.spiffocode.sigesapi.users.domain.exception.StudentRegistrationNumberExistsException;
import dev.spiffocode.sigesapi.users.domain.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class StudentUniquenessValidator {

    private final StudentRepository studentRepository;

    @WithDeletedRecords
    public void assertRegisterUnique(String registrationNumber){
        if(studentRepository.existsByRegistrationNumber(registrationNumber)){
            throw new StudentRegistrationNumberExistsException(registrationNumber);
        }
    }

    @WithDeletedRecords
    public void assertUpdateUnique(Long entityId, String registrationNumber){
        if(studentRepository.existsByRegistrationNumberAndIdNot(registrationNumber, entityId)){
            throw new StudentRegistrationNumberExistsException(registrationNumber);
        }
    }
}
