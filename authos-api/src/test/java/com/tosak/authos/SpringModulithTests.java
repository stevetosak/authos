package com.tosak.authos;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

public class SpringModulithTests {
    ApplicationModules applicationModules = ApplicationModules.of(AuthosApplication.class);
    @Test
    void modulesCompliant(){
        applicationModules.verify();
    }

    @Test
    void writeDocumentationSnippets() {
        new Documenter(applicationModules)
                .writeModulesAsPlantUml()
                .writeIndividualModulesAsPlantUml();
    }
}

