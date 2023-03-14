package com.coeux.todo.common;

import dasniko.testcontainers.keycloak.KeycloakContainer;

public class CommonKeycloakContainer{

    private KeycloakContainer keycloak;

    CommonKeycloakContainer(){
        keycloak = new KeycloakContainer().withRealmImportFile("realm-export.json");
    }
    
    public void start() {
        keycloak.start();
    }

    public void stop() {
        keycloak.stop();
    }
    
}
