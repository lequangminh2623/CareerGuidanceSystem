package com.lqm.validators;

import com.lqm.models.AcademicYear;
import com.lqm.models.Classroom;
import com.lqm.models.Course;
import com.lqm.models.ForumPost;
import com.lqm.models.ForumReply;
import com.lqm.models.Semester;
import com.lqm.models.GradeDetail;
import com.lqm.models.User;
import com.lqm.dtos.ForumPostDTO;
import com.lqm.dtos.ForumReplyDTO;
import com.lqm.dtos.TranscriptDTO;
import com.lqm.dtos.UserDTO;
import jakarta.validation.ConstraintViolation;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class WebAppValidator implements Validator {

    @Autowired
    private jakarta.validation.Validator beanValidator;

    @Setter
    private Set<Validator> springValidators = new HashSet<>();

    @Override
    public boolean supports(Class<?> clazz) {
        if (TranscriptDTO.class.isAssignableFrom(clazz)) {
            return true;
        }

        if (List.class.isAssignableFrom(clazz)) {
            return true;
        }

        for (Validator v : springValidators) {
            if (v.supports(clazz)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void validate(Object target, Errors errors) {
        Set<ConstraintViolation<Object>> constraintViolations = beanValidator.validate(target);
        for (ConstraintViolation<Object> violation : constraintViolations) {
            errors.rejectValue(violation.getPropertyPath().toString(), violation.getMessageTemplate(), violation.getMessage());
        }

        if (target instanceof Classroom) {
            for (Validator validator : springValidators) {
                if (validator instanceof ClassroomValidator) {
                    validator.validate(target, errors);
                }
            }
        } else if (target instanceof User) {
            for (Validator validator : springValidators) {
                if (validator instanceof UserValidator) {
                    validator.validate(target, errors);
                }
            }
        } else if (target instanceof Course) {
            for (Validator validator : springValidators) {
                if (validator instanceof CourseValidator) {
                    validator.validate(target, errors);
                }
            }
        } else if (target instanceof AcademicYear) {
            for (Validator validator : springValidators) {
                if (validator instanceof AcademicYearValidator) {
                    validator.validate(target, errors);
                }
            }
        } else if (target instanceof Semester) {
            for (Validator validator : springValidators) {
                if (validator instanceof SemesterValidator) {
                    validator.validate(target, errors);
                }
            }
        } else if (target instanceof ForumPost) {
            for (Validator validator : springValidators) {
                if (validator instanceof ForumPostValidator) {
                    validator.validate(target, errors);
                }
            }
        } else if (target instanceof ForumReply) {
            for (Validator validator : springValidators) {
                if (validator instanceof ForumReplyValidator) {
                    validator.validate(target, errors);
                }
            }
        } else if (target instanceof GradeDetail) {
            for (Validator validator : springValidators) {
                if (validator instanceof GradeValidator) {
                    validator.validate(target, errors);
                }
            }
        } else if (target instanceof UserDTO) {
            for (Validator validator : springValidators) {
                if (validator instanceof UserDTOValidator) {
                    validator.validate(target, errors);
                }
            }
        } else if (target instanceof ForumPostDTO) {
            for (Validator validator : springValidators) {
                if (validator instanceof ForumPostDTOValidator) {
                    validator.validate(target, errors);
                }
            }
        } else if (target instanceof ForumReplyDTO) {
            for (Validator validator : springValidators) {
                if (validator instanceof ForumReplyDTOValidator) {
                    validator.validate(target, errors);
                }
            }
        }
    }

}
