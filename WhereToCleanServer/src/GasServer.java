import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;

public class GasServer {

   public static void main(String[] args) {

      // DB���ð�ü
      Connection conn = null;
      PreparedStatement pstmt = null;
      ResultSet rs = null;
      // ��Ʈ��ȣ
      int port = 8888;
      // �����ͱ׷����ϰ�ü
      DatagramSocket socket = null;
      // //���糯¥
      // Date date = new Date(System.currentTimeMillis());
      // SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
      // String date2 = sdf.format(date);
      // String realdate = date2+"%";

      try {
         String jdbcUrl = "jdbc:mysql://localhost:3306/testdb";
         String jdbcId = "root";
         String jdbcPw = "rootpass";
         Class.forName("com.mysql.jdbc.Driver");
         conn = DriverManager.getConnection(jdbcUrl, jdbcId, jdbcPw);

         System.out.println("���� �������Դϴ�.");
         socket = new DatagramSocket(port);

         while (true) {
            // ������ ���� ����
            byte[] buffer = new byte[1024];
            // ���� ��Ŷ, ���Ŵ��
            DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);
            socket.receive(receivePacket);
            // ����Ȯ��
            String msg = new String(receivePacket.getData(), 0, receivePacket.getLength());
            System.out.println("���� ������ : " + msg);

            if (msg.equals("showdata")) {
               String sendmsg = "";
               String Query1 = "SELECT roomname,sum(people) FROM testtable group by roomname";
               pstmt = conn.prepareStatement(Query1);
               rs = pstmt.executeQuery();

               while (rs.next()) {
                  sendmsg += rs.getString(2) + ",";
               }
               sendmsg = sendmsg+"a";
               String Query6 = "select gasdata from (select * from testtable2 order by date desc) as a group by roomname";
               pstmt = conn.prepareStatement(Query6);
               rs = pstmt.executeQuery();
               while (rs.next()) {
                  sendmsg += rs.getString(1) + ",";
               }

               byte[] sendbuffer = sendmsg.getBytes();
               // System.out.println(sendmsg);
               // System.out.println(sendbuffer.length);
               DatagramPacket sendall = new DatagramPacket(sendbuffer, sendbuffer.length,
                     receivePacket.getAddress(), receivePacket.getPort());
               socket.send(sendall);
               pstmt.close();
               rs.close();
            } else if (msg.equals("showreq")) {
               String sendmsg = "";
               String Query2 = "SELECT distinct roomname FROM testtable WHERE req=1";
               pstmt = conn.prepareStatement(Query2);
               rs = pstmt.executeQuery();

               while (rs.next()) {
                  sendmsg += rs.getString(1) + ",";
               }

               byte[] sendbuffer = sendmsg.getBytes();
               // System.out.println(sendmsg);
               // System.out.println(sendbuffer.length);
               DatagramPacket sendall = new DatagramPacket(sendbuffer, sendbuffer.length,
                     receivePacket.getAddress(), receivePacket.getPort());
               socket.send(sendall);
               pstmt.close();
               rs.close();
            } else {
               String[] divmsg = msg.split(",");
               if (divmsg[1].equals("1")) {
                  String Query3 = "DELETE FROM testtable WHERE roomname=" + divmsg[0];
                  pstmt = conn.prepareStatement(Query3);
                  pstmt.executeUpdate();
                  String Query4 = "INSERT INTO testtable (roomname, people) VALUES (" + divmsg[0] + ",0)";
                  pstmt = conn.prepareStatement(Query4);
                  pstmt.executeUpdate();
                  pstmt.close();
               } else {
                  String Query5 = "UPDATE testtable SET req=1 WHERE roomname="+divmsg[0];
                  pstmt = conn.prepareStatement(Query5);
                  pstmt.executeUpdate();
                  pstmt.close();
               }
            }
         }
      } catch (Exception e) {
         System.out.println(e);
      } finally {
         if (socket != null)
            socket.close();
      }

   }

}