package com.gestion.bibliotheque;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import io.camunda.client.CamundaClient;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:contexte;MODE=MySQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "camunda.client.enabled=false"
})
class GestionBibliothequeBackendApplicationTests {
    @MockitoBean
    CamundaClient camundaClient;

	@Test
	void contextLoads() {
	}

}
