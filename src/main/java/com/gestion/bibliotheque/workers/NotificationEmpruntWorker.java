package com.gestion.bibliotheque.workers;

import io.camunda.client.annotation.JobWorker;
import io.camunda.client.annotation.Variable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class NotificationEmpruntWorker {

    private static final Logger log = LoggerFactory.getLogger(NotificationEmpruntWorker.class);

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String expediteur;

    public NotificationEmpruntWorker(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Execute la tache de service "envoyer-notification-emprunt" du BPMN.
     * Si l'envoi echoue, l'exception fait echouer le job (Camunda le retente, puis cree un incident).
     */
    @JobWorker(type = "envoyer-notification-emprunt")
    public void envoyerNotification(@Variable String emailMembre,
                                    @Variable String nomMembre,
                                    @Variable String titreLivre,
                                    @Variable String dateRetourPrevue) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(expediteur);
        message.setTo(emailMembre);
        message.setSubject("Confirmation de votre emprunt : " + titreLivre);
        message.setText("Bonjour " + nomMembre + ",\n\n"
                + "Votre emprunt du livre \"" + titreLivre + "\" a bien ete enregistre.\n"
                + "Merci de le rapporter avant le " + dateRetourPrevue + ".\n\n"
                + "Cordialement,\nLa bibliotheque");
        mailSender.send(message);
        log.info("Notification d'emprunt envoyee a {}", emailMembre);
    }
}
