// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.administration;

import java.util.Base64;
import java.util.UUID;

import org.springframework.stereotype.Service;

@Service
public class OneTimeTokenGenerator {

    public String generateNewOneTimeToken() {
        /* UUID uses SecureRandom... */
        String uuidAsString = UUID.randomUUID().toString();
        return Base64.getEncoder().encodeToString(uuidAsString.getBytes());
    }

}
