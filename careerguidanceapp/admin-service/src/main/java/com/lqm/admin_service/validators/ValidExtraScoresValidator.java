package com.lqm.admin_service.validators;

import com.lqm.admin_service.annotations.ValidExtraScores;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.List;

public class ValidExtraScoresValidator implements ConstraintValidator<ValidExtraScores, List<Double>> {

    @Override
    public boolean isValid(List<Double> extraGrades, ConstraintValidatorContext context) {
        if (extraGrades == null) {
            return true; 
        }
        for (Double grade : extraGrades) {
            if (grade != null && (grade < 0.0 || grade > 10.0)) {
                return false;
            }
        }
        return true;
    }
}
