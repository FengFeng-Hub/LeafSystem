package leaf.common.net;

import com.sun.mail.util.MailSSLSocketFactory;
import leaf.common.Config;
import leaf.common.Log;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.security.GeneralSecurityException;
import java.util.Properties;

/**
 * 邮箱操作类
 * 依赖 mail-x.x.x.jar activation-1.1.1.jar
 */
public class Mail {
    private static String FROM_EMAIL;
    private static Session SESSION;

    /**
     * 配置Mail
     * @param host 指定发送邮件的主机
     * @param port 端口号
     * @param fromEmail 发件人电子邮箱
     * @param password 密码
     */
    public static void config(String host,String port,String fromEmail,String password) {
        FROM_EMAIL = fromEmail;
        Properties properties = System.getProperties();//获取系统属性
        properties.setProperty("mail.smtp.host", host);//设置邮件服务器
        properties.setProperty("mail.smtp.port", port);//端口
//        properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        properties.put("mail.smtp.ssl.protocols", "TLSv1.2");//指定协议
        MailSSLSocketFactory sf = null;//设置SSL加密

        try {
            sf = new MailSSLSocketFactory();
        } catch(GeneralSecurityException e) {
            Log.write("Error_Mail",Log.getException(e));
        }

        sf.setTrustAllHosts(true);
        properties.put("mail.smtp.ssl.enable", "true");
        properties.put("mail.smtp.ssl.socketFactory", sf);
        properties.put("mail.smtp.auth", "true");
        // 获取默认session对象
        SESSION = Session.getDefaultInstance(properties,new Authenticator(){
            public PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromEmail, password);//发件人邮件用户名、授权码
            }
        });
    }

    /**
     * 发送邮件
     * @param toEmail 接收人的邮箱
     * @param title 头部消息
     * @param content 消息体
     * @return true发送成功
     */
    public static boolean sendEmail(String toEmail, String title, String content) {
        try{
            MimeMessage message = new MimeMessage(SESSION);//创建默认的 MimeMessage 对象
            message.setFrom(new InternetAddress(FROM_EMAIL));// et From:头部头字段
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));//Set To:头部头字段
            message.setSubject(title);//Set Subject:头部头字段
            message.setText(content);//设置消息体
            Transport.send(message);//发送消息
            return true;
        } catch(MessagingException e) {
            Log.write("Error_Mail",Log.getException(e));
            return false;
        }
    }
}
