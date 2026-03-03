package dev.spiffocode.sigesapi.users.infrastructure.service.impl;

import dev.spiffocode.sigesapi.common.infrastructure.persistence.WithDeletedRecords;
import dev.spiffocode.sigesapi.users.domain.exception.EmployeeNumberExistsException;
import dev.spiffocode.sigesapi.users.domain.repository.InstitutionalStaffRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class InstitutionalStaffUniquenessValidator {

    private final InstitutionalStaffRepository institutionalStaffRepository;

    @WithDeletedRecords
    public void assertRegisterUnique(String employeeNumber){
        if(institutionalStaffRepository.existsByEmployeeNumber(employeeNumber)){
            throw new EmployeeNumberExistsException(employeeNumber);
        }
    }

    @WithDeletedRecords
    public void assertUpdateUnique(Long entityId, String employeeNumber){
        if(institutionalStaffRepository.existsByEmployeeNumberAndIdNot(employeeNumber, entityId)){
            throw new EmployeeNumberExistsException(employeeNumber);
        }
    }
}
