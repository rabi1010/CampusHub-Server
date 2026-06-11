package com.example.campus_hub.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import jakarta.mail.internet.MimeMessage;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Async
    public void sendRegistrationEmail(String toEmail, String fullName) {
        try {
            MimeMessage  message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    message, true, "UTF-8"
            );
            helper.setFrom("CampusHub <" + fromEmail + ">");
            helper.setTo(toEmail);
            helper.setSubject(
                    "Welcome to CampusHub — Account Pending Approval"
            );
            helper.setText(
                    buildRegistrationHtml(fullName, toEmail), true
            );
            mailSender.send(message);
            log.info("Registration email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Email failed: {}", e.getMessage());
        }
    }
    @Async
    public void sendStudentCreatedEmail(
            String toEmail, String fullName, String rollNo
    ) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    message, true, "UTF-8"
            );
            helper.setFrom("CampusHub <" + fromEmail + ">");
            helper.setTo(toEmail);
            helper.setSubject("CampusHub — Your Student Account Is Ready");
            helper.setText(buildStudentCreatedHtml(fullName, rollNo, toEmail), true);
            mailSender.send(message);
            log.info("Student created email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Email failed: {}", e.getMessage());
        }
    }

    private String buildStudentCreatedHtml(String fullName, String rollNo, String toEmail) {
        return """
        <html><body style="font-family:Arial;background:#f5f5f5">
        <div style="max-width:560px;margin:40px auto;background:#fff;
                    border-radius:16px;overflow:hidden">
          <div style="background:#0d0d1f;padding:32px;text-align:center">
            <h1 style="color:#3aba84;margin:0">CampusHub</h1>
          </div>
          <div style="padding:32px">
            <h2>Welcome, %s! 🎓</h2>
            <p>Your student account has been created by the administrator.</p>
            <div style="background:#edfaf4;border:1px solid #a3e7c9;
                        border-radius:10px;padding:16px;margin:20px 0">
              <p><strong>Roll Number:</strong> %s</p>
              <p><strong>Email:</strong> %s</p>
              <p>Use the password provided by your administrator to login.</p>
            </div>
            <div style="text-align:center;margin:24px 0">
              <a href="http://localhost:5173/login"
                 style="background:#1f9d68;color:#fff;padding:14px 32px;
                        border-radius:10px;text-decoration:none">
                Sign in to CampusHub →
              </a>
            </div>
          </div>
          <div style="background:#f8f8f8;padding:20px;
                      text-align:center;font-size:12px;color:#999">
            © %d CampusHub
          </div>
        </div>
        </body></html>
        """.formatted(
                fullName,
                rollNo,
                toEmail,
                java.time.Year.now().getValue()
        );
    }
    @Async
    public void sendApprovalEmail(
            String toEmail, String fullName, String role
    ) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    message, true, "UTF-8"
            );
            helper.setFrom("CampusHub <" + fromEmail + ">");
            helper.setTo(toEmail);
            helper.setSubject("CampusHub — Your Account Is Approved!");
            helper.setText(buildApprovalHtml(fullName, role), true);
            mailSender.send(message);
            log.info("Approval email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Email failed: {}", e.getMessage());
        }
    }

    @Async
    public void sendParentCreatedEmail(
        String toEmail,
        String parentName,
        List<String> childNames
    ) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                message, true, "UTF-8"
            );
            helper.setFrom("CampusHub <" + fromEmail + ">");
            helper.setTo(toEmail);
            helper.setSubject("CampusHub — Your Parent Account Is Ready");
            helper.setText(
                buildParentCreatedHtml(parentName, childNames), true
            );
            mailSender.send(message);
            log.info("Parent created email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Email failed: {}", e.getMessage());
        }
    }

    private String buildParentCreatedHtml(
        String parentName,
        List<String> childNames
    ) {
        String childList = childNames.stream()
            .map(name -> "<li>" + name + "</li>")
            .reduce("", String::concat);

        return """
            <html><body style="font-family:Arial;background:#f5f5f5">
            <div style="max-width:560px;margin:40px auto;background:#fff;
                        border-radius:16px;overflow:hidden">
              <div style="background:#0d0d1f;padding:32px;text-align:center">
                <h1 style="color:#3aba84;margin:0">CampusHub</h1>
              </div>
              <div style="padding:32px">
                <h2>Welcome, %s! 👨‍👩‍👧</h2>
                <p>Your parent account has been created.
                   You can now monitor your child's progress.</p>
                <div style="background:#edfaf4;border:1px solid #a3e7c9;
                            border-radius:10px;padding:16px;margin:20px 0">
                  <p><strong>Linked children:</strong></p>
                  <ul>%s</ul>
                </div>
                <div style="text-align:center;margin:24px 0">
                  <a href="http://localhost:5173/login"
                     style="background:#1f9d68;color:#fff;padding:14px 32px;
                            border-radius:10px;text-decoration:none">
                    Sign in to CampusHub →
                  </a>
                </div>
              </div>
              <div style="background:#f8f8f8;padding:20px;
                          text-align:center;font-size:12px;color:#999">
                © %d CampusHub
              </div>
            </div>
            </body></html>
            """.formatted(
                parentName,
                childList,
                java.time.Year.now().getValue()
            );
    }

    private String buildRegistrationHtml(
            String fullName, String email
    ) {
        return """
            <html><body style="font-family:Arial;background:#f5f5f5">
            <div style="max-width:560px;margin:40px auto;background:#fff;
                        border-radius:16px;overflow:hidden">
              <div style="background:#0d0d1f;padding:32px;text-align:center">
                <h1 style="color:#3aba84;margin:0">CampusHub</h1>
              </div>
              <div style="padding:32px">
                <h2>Hello, %s! 👋</h2>
                <p>Thank you for registering on CampusHub.</p>
                <div style="background:#edfaf4;border:1px solid #a3e7c9;
                            border-radius:10px;padding:16px;margin:20px 0">
                  <p>⏳ <strong>Account pending approval.</strong>
                  Admin will activate your account shortly.</p>
                </div>
                <p>Email: <strong>%s</strong></p>
              </div>
              <div style="background:#f8f8f8;padding:20px;
                          text-align:center;font-size:12px;color:#999">
                © %d CampusHub
              </div>
            </div>
            </body></html>
            """.formatted(fullName, email,
                java.time.Year.now().getValue());
    }

    private String buildApprovalHtml(String fullName, String role) {
        return """
            <html><body style="font-family:Arial;background:#f5f5f5">
            <div style="max-width:560px;margin:40px auto;background:#fff;
                        border-radius:16px;overflow:hidden">
              <div style="background:#0d0d1f;padding:32px;text-align:center">
                <h1 style="color:#3aba84;margin:0">CampusHub</h1>
              </div>
              <div style="padding:32px">
                <h2>Great news, %s! 🎉</h2>
                <p>Your account has been approved as
                   <strong>%s</strong>.</p>
                <div style="text-align:center;margin:24px 0">
                  <a href="http://localhost:5173/login"
                     style="background:#1f9d68;color:#fff;padding:14px 32px;
                            border-radius:10px;text-decoration:none">
                    Sign in to CampusHub →
                  </a>
                </div>
              </div>
              <div style="background:#f8f8f8;padding:20px;
                          text-align:center;font-size:12px;color:#999">
                © %d CampusHub
              </div>
            </div>
            </body></html>
            """.formatted(fullName, role.toLowerCase(),
                java.time.Year.now().getValue());
    }
    @Async
    public void sendTeacherCreatedEmail(
            String toEmail,
            String fullName,
            String employeeId
    ) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    message, true, "UTF-8"
            );
            helper.setFrom("CampusHub <" + fromEmail + ">");
            helper.setTo(toEmail);
            helper.setSubject(
                    "CampusHub — Your Teacher Account Is Ready"
            );
            helper.setText(
                    buildTeacherCreatedHtml(fullName, employeeId), true
            );
            mailSender.send(message);
            log.info("Teacher created email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Email failed: {}", e.getMessage());
        }
    }

    private String buildTeacherCreatedHtml(
            String fullName, String employeeId
    ) {
        return """
        <html><body style="font-family:Arial;background:#f5f5f5">
        <div style="max-width:560px;margin:40px auto;background:#fff;
                    border-radius:16px;overflow:hidden">
          <div style="background:#0d0d1f;padding:32px;text-align:center">
            <h1 style="color:#3aba84;margin:0">CampusHub</h1>
          </div>
          <div style="padding:32px">
            <h2>Welcome, %s! 👨‍🏫</h2>
            <p>Your teacher account has been created.</p>
            <div style="background:#edfaf4;border:1px solid #a3e7c9;
                        border-radius:10px;padding:16px;margin:20px 0">
              <p><strong>Employee ID:</strong> %s</p>
              <p>Use the password provided to login.</p>
            </div>
            <div style="text-align:center;margin:24px 0">
              <a href="http://localhost:5173/login"
                 style="background:#1f9d68;color:#fff;
                        padding:14px 32px;border-radius:10px;
                        text-decoration:none">
                Sign in to CampusHub →
              </a>
            </div>
          </div>
          <div style="background:#f8f8f8;padding:20px;
                      text-align:center;font-size:12px;color:#999">
            © %d CampusHub
          </div>
        </div>
        </body></html>
        """.formatted(
                fullName,
                employeeId,
                java.time.Year.now().getValue()
        );
    }
    @Async
    public void sendAbsenceNotificationEmail(
            String toEmail,
            String parentName,
            String studentName,
            String courseName,
            String date
    ) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    message, true, "UTF-8"
            );
            helper.setFrom("CampusHub <" + fromEmail + ">");
            helper.setTo(toEmail);
            helper.setSubject(
                    "⚠️ Attendance Alert — " + studentName +
                            " was absent today"
            );
            helper.setText(
                    buildAbsenceHtml(
                            parentName, studentName, courseName, date
                    ), true
            );
            mailSender.send(message);
            log.info("Absence notification sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Absence email failed: {}", e.getMessage());
        }
    }

    private String buildAbsenceHtml(
            String parentName,
            String studentName,
            String courseName,
            String date
    ) {
        return """
        <html><body style="font-family:Arial;background:#f5f5f5">
        <div style="max-width:560px;margin:40px auto;background:#fff;
                    border-radius:16px;overflow:hidden">
          <div style="background:#0d0d1f;padding:32px;text-align:center">
            <h1 style="color:#3aba84;margin:0">CampusHub</h1>
          </div>
          <div style="padding:32px">
            <h2>Dear %s,</h2>
            <div style="background:#fef2f2;border:1px solid #fecaca;
                        border-radius:10px;padding:16px;margin:20px 0">
              <p>⚠️ <strong>Attendance Alert</strong></p>
              <p>Your child <strong>%s</strong> was marked
                 <strong style="color:#dc2626">ABSENT</strong>
                 in the following class:</p>
              <p><strong>Course:</strong> %s</p>
              <p><strong>Date:</strong> %s</p>
            </div>
            <p>If you have any questions, please contact
               the college directly.</p>
            <div style="text-align:center;margin:24px 0">
              <a href="http://localhost:5173/login"
                 style="background:#1f9d68;color:#fff;
                        padding:14px 32px;border-radius:10px;
                        text-decoration:none">
                View Attendance →
              </a>
            </div>
          </div>
          <div style="background:#f8f8f8;padding:20px;
                      text-align:center;font-size:12px;color:#999">
            © %d CampusHub
          </div>
        </div>
        </body></html>
        """.formatted(
                parentName,
                studentName,
                courseName,
                date,
                java.time.Year.now().getValue()
        );
    }
}