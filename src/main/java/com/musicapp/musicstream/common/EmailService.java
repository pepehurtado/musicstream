package com.musicapp.musicstream.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender emailSender;

    public void sendActivationEmail(String username, String mail, String activationLink) {
        String subject = "Activa tu cuenta de Music Stream";
    
        String imageCid = "logoImage"; // Content-ID for the image
        String stringCid = "stringImage";
    
        String htmlContent = "<!DOCTYPE html>" +
            "<html lang='es'>" +
            "<head>" +
            "<meta charset='UTF-8'>" +
            "<meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
            "<title>Activación de Cuenta</title>" +
            "<style>" +
            "body { font-family: Arial, sans-serif; background-color: #f4f4f4; color: #333; margin: 0; padding: 0; }" +
            ".container { width: 80%; margin: auto; padding: 20px; background: #fff; border-radius: 8px; box-shadow: 0 0 10px rgba(0, 0, 0, 0.1); }" +
            ".header { text-align: center; margin-bottom: 20px; }" +
            ".header img { max-width: 100px; }" +
            ".content { text-align: center; }" +
            ".button { display: inline-block; padding: 10px 20px; font-size: 16px; color: #fff !important; background-color: #007bff !important; border-radius: 5px; text-decoration: none; margin-top: 20px; cursor: pointer; }" +
            ".button:hover { background-color: #0056b3 !important; }" +
            ".footer { text-align: center; margin-top: 20px; font-size: 14px; color: #777; }" +
            "</style>" +
            "</head>" +
            "<body>" +
            "<div class='container'>" +
            "<div class='header'>" +
            "<div>" +
            "<img src='cid:" + stringCid + "' alt='Logo de la Empresa'>" +
            "</div>" +
            "<div>" +
            "<img src='cid:" + imageCid + "' alt='Logo de la Empresa'>" +
            "</div>" +
            "<h1>Activa tu cuenta</h1>" +
            "</div>" +
            "<div class='content'>" +
            "<p>Hola " + username + ",</p>" +
            "<p>Gracias por registrarte con nosotros. Para activar tu cuenta, por favor haz clic en el botón de abajo:</p>" +
            "<a href='" + activationLink + "' class='button'>Activar Cuenta</a>" +
            "<p>o haz clic en el siguiente enlace: <a href='" + activationLink + "' style='color: #007bff; text-decoration: underline;'>" + activationLink + "</a></p>" +
            "</div>" +
            "<div class='footer'>" +
            "<p>Si no solicitaste esto, por favor ignora este correo electrónico.</p>" +
            "</div>" +
            "</div>" +
            "</body>" +
            "</html>";
    
        MimeMessage message = emailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom("jose.hurtadog@outlook.com");
            helper.setTo(mail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true); // Set to true to send HTML

            // Adjuntar la imagen JPEG con Content-ID
            ClassPathResource imageResource = new ClassPathResource("images/logo.jpeg");
            ClassPathResource stringResource = new ClassPathResource("images/logo-text.jpeg");
            helper.addInline(imageCid, imageResource);
            helper.addInline(stringCid, stringResource);

            emailSender.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}
