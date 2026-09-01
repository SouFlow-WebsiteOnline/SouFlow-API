package com.souflow.models.services.impl;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.souflow.models.entities.Order;
import com.souflow.models.entities.OrderDetail;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.from:${MAIL_FROM:no-reply@souflow.shop}}")
    private String mailFrom;

    @Value("${spring.mail.from-name:${MAIL_FROM_NAME:SouFlow Botanical Artistry}}")
    private String mailFromName;
    
    @Async
    public void sendEmailWithInlineImage(String to, Order order) throws Exception { //asynchronous with js
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(mailFrom, mailFromName);
        helper.setTo(to);
        helper.setSubject("Thông tin đơn hàng");

        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));

        // Build order details rows
        StringBuilder rows = new StringBuilder();
        int index = 1;

        for (OrderDetail od : order.getOrderDetails()) {
            rows.append("<tr>")
                .append("<td style='border:1px solid #ddd;padding:8px;text-align:center;'>").append(index++).append("</td>")
                .append("<td style='border:1px solid #ddd;padding:8px;text-align:left;'>").append(od.getProduct().getNameVn()).append("</td>")
                .append("<td style='border:1px solid #ddd;padding:8px;text-align:center;'>")
                .append(formatter.format(od.getProduct().getPrice())).append(" VND</td>")
                .append("<td style='border:1px solid #ddd;padding:8px;text-align:center;'>")
                .append(od.getQuantity()).append("</td>")
                .append("<td style='border:1px solid #ddd;padding:8px;text-align:center;'>")
                .append(formatter.format(od.getProduct().getPrice().multiply(new BigDecimal(od.getQuantity())))).append(" VND</td>")
                .append("</tr>");
        }

        String htmlContent = """
        <div style="font-family:Arial, sans-serif; max-width:700px; margin:auto; border:1px solid #ddd;">
            
            <!-- Header -->
            <div style="background:#212529; color:white; padding:10px; text-align:center;">
                <img src="cid:logoImage" width="120"/><br/>
                <h2 style="margin:5px 0;">Thông tin đơn hàng</h2>
            </div>

            <!-- Customer Info -->
            <div style="padding:15px;">
                <h3 style="margin-bottom:10px;">Thông tin khách hàng</h3>
                <p><b>Họ tên:</b> %s</p>
                <p><b>Tài khoản:</b> %s</p>
                <p><b>Địa chỉ:</b> %s</p>
                <p><b>Điện thoại:</b> %s</p>
            </div>

            <!-- Order Table -->
            <div style="padding:15px;">
                <table style="width:100%%; border-collapse:collapse;">
                    <thead>
                        <tr style="background:#212529; color:white;">
                            <th style="padding:8px;border:1px solid #ddd;">#</th>
                            <th style="padding:8px;border:1px solid #ddd;">Tên sản phẩm</th>
                            <th style="padding:8px;border:1px solid #ddd;">Đơn giá</th>
                            <th style="padding:8px;border:1px solid #ddd;">Số lượng</th>
                            <th style="padding:8px;border:1px solid #ddd;">Tổng phụ</th>
                        </tr>
                    </thead>
                    <tbody>
                        %s
                        <tr style="background:#f2f2f2; font-weight:bold;">
                            <td colspan="4" style="padding:8px;border:1px solid #ddd; text-align:right;">
                                Tổng tiền:
                            </td>
                            <td style="padding:8px;border:1px solid #ddd; color:red; text-align:center;">
                                %s VND
                            </td>
                        </tr>
                    </tbody>
                </table>
            </div>
        </div>
        """.formatted(
                order.getAccount().getFullname(),
                order.getAccount().getUsername(),
                order.getAccount().getAddress(),
                order.getAccount().getPhone(),
                rows.toString(),
                formatter.format(order.getTotal())
        );

        helper.setText(htmlContent, true);

        // Inline image
        ClassPathResource image = new ClassPathResource("static/images/people_in_1801_bc.jpg");
        helper.addInline("logoImage", image);

        mailSender.send(message);
    }

    @Async
    public void sendOtpEmail(String to, String otp) throws Exception {
        MimeMessage message = mailSender.createMimeMessage();
        message.setHeader("Auto-Submitted", "auto-generated");
        message.setHeader("X-Auto-Response-Suppress", "All");
        
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom(mailFrom, mailFromName);
        helper.setTo(to);
        helper.setSubject("[SouFlow] Mã xác thực đặt lại mật khẩu");

        String plainText = """
        SouFlow - Mã xác thực đặt lại mật khẩu
        
        Mã xác thực OTP của bạn là: %s
        Mã này có hiệu lực trong vòng 5 phút. Vui lòng không chia sẻ mã này cho bất kỳ ai.
        
        SouFlow • Tinh hoa hoa tươi nghệ thuật & quà tặng cao cấp
        Website: souflow.shop
        """.formatted(otp);

        String htmlContent = """
        <div style="font-family:Arial, sans-serif; max-width:500px; margin:auto; border:1px solid #ddd; padding: 20px; text-align: center;">
            <h2 style="color: #212529;">Yêu cầu đặt lại mật khẩu</h2>
            <p>Xin chào,</p>
            <p>Bạn đã yêu cầu đặt lại mật khẩu. Vui lòng sử dụng mã OTP dưới đây để xác thực:</p>
            <div style="background-color: #f8f9fa; padding: 15px; margin: 20px 0; font-size: 24px; font-weight: bold; letter-spacing: 5px; color: #007bff; border-radius: 5px;">
                %s
            </div>
            <p style="color: #6c757d; font-size: 14px;">Mã OTP này có hiệu lực trong vòng 5 phút. Vui lòng không chia sẻ mã này với bất kỳ ai.</p>
            <p>Nếu bạn không thực hiện yêu cầu này, vui lòng bỏ qua email này.</p>
            <hr style="border: none; border-top: 1px solid #eee; margin: 20px 0;" />
            <p style="font-size: 12px; color: #999;">Trân trọng,<br/>Đội ngũ SouFlow</p>
        </div>
        """.formatted(otp);

        helper.setText(plainText, htmlContent);
        mailSender.send(message);
    }

    @Async
    public void sendRegisterOtpEmail(String to, String otp) throws Exception {
        MimeMessage message = mailSender.createMimeMessage();
        message.setHeader("Auto-Submitted", "auto-generated");
        message.setHeader("X-Auto-Response-Suppress", "All");

        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom(mailFrom, mailFromName);
        helper.setTo(to);
        helper.setSubject("[SouFlow] Mã xác thực đăng ký tài khoản");

        String plainText = """
        SouFlow Botanical Artistry - Xác thực tạo tài khoản
        
        Chào mừng bạn đến với SouFlow!
        Mã xác thực OTP đăng ký của bạn là: %s
        
        Mã OTP này có hiệu lực trong vòng 5 phút. Vui lòng không chia sẻ cho bất kỳ ai.
        
        SouFlow • Tinh hoa hoa tươi nghệ thuật & quà tặng cao cấp
        Hotline: 0901 234 567 | Website: souflow.shop
        """.formatted(otp);

        String htmlContent = """
        <div style="font-family: 'Segoe UI', Arial, sans-serif; max-width: 520px; margin: auto; border: 1px solid #EBE5DA; border-radius: 16px; overflow: hidden; background: #FAF7F2;">
            <div style="background: #2D2825; color: #FFFFFF; padding: 24px; text-align: center;">
                <h1 style="margin: 0; font-family: Georgia, serif; font-size: 24px; font-weight: normal; letter-spacing: 2px;">SOUFLOW</h1>
                <p style="margin: 4px 0 0 0; font-size: 11px; text-transform: uppercase; letter-spacing: 2px; color: #C49B83;">Botanical Artistry</p>
            </div>
            <div style="padding: 28px 24px; text-align: center; color: #2D2825;">
                <h2 style="font-family: Georgia, serif; font-size: 20px; margin-top: 0; color: #2D2825;">Xác Thực Tạo Tài Khoản</h2>
                <p style="font-size: 14px; line-height: 1.6; color: #5C5550; margin: 12px 0 20px 0;">
                    Chào mừng bạn đến với <b>SouFlow</b>. Để hoàn tất đăng ký và mở khóa tính năng đặt hoa & thanh toán, vui lòng nhập mã xác thực OTP dưới đây:
                </p>
                <div style="background: #FFFFFF; border: 2px dashed #C49B83; padding: 18px 24px; margin: 20px auto; font-size: 30px; font-weight: bold; letter-spacing: 8px; color: #9E6B55; border-radius: 12px; display: inline-block;">
                    %s
                </div>
                <p style="color: #8C847E; font-size: 13px; margin: 16px 0 0 0;">
                    ⏳ Mã OTP này có hiệu lực trong vòng <b>5 phút</b>. Vui lòng không chia sẻ mã này cho bất kỳ ai.
                </p>
            </div>
            <div style="background: #F2ECE1; padding: 16px 24px; text-align: center; font-size: 12px; color: #8C847E; border-top: 1px solid #EBE5DA;">
                SouFlow • Tinh hoa hoa tươi nghệ thuật & quà tặng cao cấp<br/>
                Hotline hỗ trợ: 0901 234 567 | Website: souflow.shop
            </div>
        </div>
        """.formatted(otp);

        helper.setText(plainText, htmlContent);
        mailSender.send(message);
    }
}
