package com.worldcup;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

public class Player_Test {

    // 1. khởi tạo Player
    @Test
    public void playerConstructor_KhoiTaoDungGiaTri() {
        Player player = new Player("Messi", 10, "Forward", "Argentina");
        assertEquals("Messi", player.getName());
        assertEquals(10, player.getNumber());
        assertEquals("Forward", player.getPosition());
        assertEquals("Argentina", player.getTeamName());
        assertEquals(0, player.getGoals());
        assertEquals(0, player.getYellowCards());
        assertEquals(0, player.getRedCards());
    }

    // 2. ghi bàn
    @Test
    public void scoreGoal_Ghi1Ban_GoalsTang1() {
        Player player = new Player("Ronaldo", 7, "Striker", "Portugal");
        player.scoreGoal();
        assertEquals(1, player.getGoals());
    }

    // 3. thẻ vàng - 1 lần
    @Test
    public void addYellowCard_1Lan_TheVangTang1() {
        Player player = new Player("A", 9, "Striker", "France");
        player.addYellowCard();
        assertEquals(1, player.getYellowCards());
    }

    // 4. thẻ vàng - 2 lần
    @Test
    public void addYellowCard_2Lan_TheVangTang2() {
        Player player = new Player("B", 2, "Forward", "Italy");
        player.addYellowCard();
        player.addYellowCard();
        assertEquals(2, player.getYellowCards());
    }

    // 5. thẻ đỏ
    @Test
    public void addRedCard_1Lan_TheDoTang1() {
        Player player = new Player("C", 3, "Forward", "Croatia");
        player.addRedCard();
        assertEquals(1, player.getRedCards());
    }

    // 6. kết hợp ghi bàn + thẻ
    @Test
    public void playerActions_GhiBanVaNhanThe_DungGiaTri() {
        Player player = new Player("D", 4, "Forward", "Germany");
        player.scoreGoal();
        player.addYellowCard();
        player.addRedCard();
        assertEquals(1, player.getGoals());
        assertEquals(1, player.getYellowCards());
        assertEquals(1, player.getRedCards());
    }

    // 7. số áo valid - biên dưới
    @Test
    public void playerConstructor_SoAoBienDuoi_KhoiTaoThanhCong() {
        Player player = new Player("E", 1, "Goalkeeper", "Brazil");
        assertEquals(1, player.getNumber());
    }

    // 8. số áo valid - biên trên
    @Test
    public void playerConstructor_SoAoBienTren_KhoiTaoThanhCong() {
        Player player = new Player("F", 26, "Defense", "Brazil");
        assertEquals(26, player.getNumber());
    }

    // 9. số áo invalid - <1
    @Test
    public void playerConstructor_SoAoNhoHon1_ThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Player("G", 0, "Goalkeeper", "Brazil");
        });
    }

    // 10. số áo invalid - >26
    @Test
    public void playerConstructor_SoAoLonHon26_ThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Player("H", 27, "Goalkeeper", "Brazil");
        });
    }

    // 11. tên null
    @Test
    public void playerConstructor_TenNull_KhoiTaoVanThanhCong() {
        Player player = new Player(null, 5, "Defense", "Poland");
        assertNull(player.getName());
    }

    // 12. position null
    @Test
    public void playerConstructor_PositionNull_KhoiTaoVanThanhCong() {
        Player player = new Player("I", 6, null, "UK");
        assertNull(player.getPosition());
    }

    // 13. teamName null
    @Test
    public void playerConstructor_TeamNameNull_KhoiTaoVanThanhCong() {
        Player player = new Player("J", 7, "Defense", null);
        assertNull(player.getTeamName());
    }

    //191. name blank- valid
    @Test
    public void PlayerConstructor_TenWhitespace_KhoiTaoThanhCong() {
        Player p = org.junit.jupiter.api.Assertions.assertDoesNotThrow(() ->
                new Player("   ", 5, "Defense", "PL"));
        org.junit.jupiter.api.Assertions.assertNotNull(p);
        org.junit.jupiter.api.Assertions.assertEquals("   ", p.getName());
    }

    //192. position ""- valid
    @Test
    public void PlayerConstructor_PositionRong_KhoiTaoThanhCong() {
        Player p = org.junit.jupiter.api.Assertions.assertDoesNotThrow(() ->
                new Player("A", 10, "", "PL"));
        org.junit.jupiter.api.Assertions.assertNotNull(p);
        org.junit.jupiter.api.Assertions.assertEquals("", p.getPosition());
    }

    //193. country ""- valid
    @Test
    public void PlayerConstructor_QuocTichRong_KhoiTaoThanhCong() {
        Player p = org.junit.jupiter.api.Assertions.assertDoesNotThrow(() ->
                new Player("A", 7, "Midfielder", ""));
        org.junit.jupiter.api.Assertions.assertNotNull(p);
        org.junit.jupiter.api.Assertions.assertEquals(7, p.getNumber());
    }

    //194. tên unicode, ký hiệu đặc biệt- valid
    @Test
    public void PlayerConstructor_TenUnicode_KhoiTaoThanhCong() {
        String uni = "Nguyễn Văn Ánh 🐉";
        Player p = new Player(uni, 13, "FW", "VN");
        org.junit.jupiter.api.Assertions.assertEquals(uni, p.getName());
    }

    //195. scoreGoal kết hợp thẻ- thống kê độc lập
    @Test
    public void ScoreGoal_KetHopTheVangDo_ThongKeDocLap() {
        Player p = new Player("B", 8, "MF", "FR");
        p.scoreGoal(); //goals=1
        p.addYellowCard(); //y=1
        p.scoreGoal(); //goals=2
        p.addRedCard(); //r=1
        org.junit.jupiter.api.Assertions.assertEquals(2, p.getGoals());
        org.junit.jupiter.api.Assertions.assertEquals(1, p.getYellowCards());
        org.junit.jupiter.api.Assertions.assertEquals(1, p.getRedCards());
    }

    //196. addYellowCard>2- check đường lặp
    @Test
    public void AddYellowCard_BaLan_Tang3() {
        Player p = new Player("C", 15, "DF", "DE");
        p.addYellowCard();
        p.addYellowCard();
        p.addYellowCard();
        org.junit.jupiter.api.Assertions.assertEquals(3, p.getYellowCards());
    }

    //197. addRedCard>1- check đg lặp
    @Test
    public void AddRedCard_HaiLan_Tang2() {
        Player p = new Player("D", 4, "GK", "IT");
        p.addRedCard();
        p.addRedCard();
        org.junit.jupiter.api.Assertions.assertEquals(2, p.getRedCards());
    }

    //198. k bị đổi các thuộc tính bất biến
    @Test
    public void ScoreGoal_KhongAnhHuongTenSoAoViTri() {
        Player p = new Player("E", 22, "MF", "ES");
        p.scoreGoal();
        org.junit.jupiter.api.Assertions.assertEquals("E", p.getName());
        org.junit.jupiter.api.Assertions.assertEquals(22, p.getNumber());
        org.junit.jupiter.api.Assertions.assertEquals("MF", p.getPosition());
    }

    //199. check độc lập trạng thái cầu thủ
    @Test
    public void HaiCauThu_TrangThaiDocLap() {
        Player a = new Player("A", 6, "FW", "BR");
        Player b = new Player("B", 14, "DF", "AR");
        a.scoreGoal();
        a.addYellowCard();
        org.junit.jupiter.api.Assertions.assertEquals(0, b.getGoals());
        org.junit.jupiter.api.Assertions.assertEquals(0, b.getYellowCards());
        org.junit.jupiter.api.Assertions.assertEquals(0, b.getRedCards());
    }

    //200. position case-sensitive
    public void PlayerConstructor_PositionCaseSensitive_GiuNguyen() {
        Player p = new Player("X", 19, "defense", "US");
        org.junit.jupiter.api.Assertions.assertEquals("defense", p.getPosition());
    }
}
