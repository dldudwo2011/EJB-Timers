/**
 * @author Youngjae Lee
 * @version 2022-03-07
 *
 * description: Bean validator
 */


package common.validation;

import jakarta.json.Json;
import jakarta.json.JsonObjectBuilder;
import jakarta.validation.Validation;

public class BeanValidator {

    public static <T> String validateBean(Class<T> classType, T typeInstance) {
        String errorMessage = null;
        var constraintViolations = Validation.buildDefaultValidatorFactory().getValidator().validate(typeInstance);
        if (constraintViolations.size() > 0) {
            JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();
            for (var singleConstraintViolation : constraintViolations) {
                jsonObjectBuilder.add(singleConstraintViolation.getPropertyPath().toString(), singleConstraintViolation.getMessage());
            }
            errorMessage = jsonObjectBuilder.build().toString();
        }
        return errorMessage;
    }
}

