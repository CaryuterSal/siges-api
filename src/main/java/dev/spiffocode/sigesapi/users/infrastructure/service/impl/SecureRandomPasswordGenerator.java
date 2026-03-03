package dev.spiffocode.sigesapi.users.infrastructure.service.impl;

import dev.spiffocode.sigesapi.users.application.service.PasswordGenerator;
import org.apache.commons.text.RandomStringGenerator;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SecureRandomPasswordGenerator implements PasswordGenerator {

    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Generates a password thats thread-safe random with
     * <ul>
     *     <li>2 numbers</li>
     *     <li>2 uppercase letters</li>
     *     <li>2 lowercase letters</li>
     *     <li>2 special characters</li>
     * </ul>
     * @return newly now encrypted password
     */
    @Override
    public String generatePassword() {
        String pwString = generateRandomSpecialCharacters(2).concat(generateRandomNumbers(2))
                .concat(generateRandomAlphabet(2, true))
                .concat(generateRandomAlphabet(2, false))
                .concat(generateRandomSpecialCharacters(2));
        List<Character> pwChars = pwString.chars()
                .mapToObj(data -> (char) data)
                .collect(Collectors.toList());
        Collections.shuffle(pwChars, secureRandom);
        return pwChars.stream()
                .map(String::valueOf)
                .collect(Collectors.joining());
    }


    private String generateRandomNumbers(int length) {
        RandomStringGenerator pwdGenerator = new RandomStringGenerator.Builder()
                .withinRange(48, 57)
                .usingRandom(secureRandom::nextInt).get();
        return pwdGenerator.generate(length);
    }

    private String generateRandomSpecialCharacters(int length) {
        RandomStringGenerator pwdGenerator = new RandomStringGenerator.Builder()
                .withinRange(33, 45)
                .usingRandom(secureRandom::nextInt)
                .get();
        return pwdGenerator.generate(length);
    }

    private String generateRandomAlphabet(int length, boolean isUpperCase) {
        char[] range = isUpperCase ? new char[]{'A', 'Z'} : new char[]{'a', 'z'};
        RandomStringGenerator pwdGenerator = new RandomStringGenerator.Builder()
                .withinRange(range)
                .usingRandom(secureRandom::nextInt)
                .get();
        return pwdGenerator.generate(length);
    }

}
