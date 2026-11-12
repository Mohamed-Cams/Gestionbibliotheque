package com.gestion.bibliotheque;

import io.camunda.client.annotation.Deployment;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
// Deploie le BPMN dans Camunda a chaque demarrage de l'application
@Deployment(resources = "classpath:bpmn/emprunt.bpmn")
public class GestionBibliothequeBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(GestionBibliothequeBackendApplication.class, args);
	}

}
