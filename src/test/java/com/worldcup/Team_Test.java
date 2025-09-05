package com.worldcup;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

public class Team_Test {

    // 14. khởi tạo team-valid
    @Test
    public void testTeamConstructor_KhoiTaoDung() {
        Team team = new Team("Vietnam", "Asia", "Coach A", "Medical A");

        assertEquals("Vietnam", team.getName());
        assertEquals("Asia", team.getRegion());
        assertEquals("Coach A", team.getCoach());
        assertEquals("Medical A", team.getMedicalStaff());
        assertEquals(0, team.getPlayers().size());
        assertEquals(0, team.getAssistantCoaches().size());
        assertEquals(0, team.getPoints());
        assertEquals(0, team.getGoalDifference());
        assertFalse(team.isHost());
    }

    // 15. tên null-invalid
    @Test
    public void testTeamConstructor_NameNull_ThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Team(null, "Asia", "Coach", "Medical");
        });
    }

    // 16. region null-invalid
    @Test
    public void testTeamConstructor_RegionNull_ThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Team("Vietnam", null, "Coach", "Medical");
        });
    }

    // 17. coach null
    @Test
    public void testTeamConstructor_CoachNull_KhoiTaoVanThanhCong() {
        Team team = new Team("VN", "Asia", null, "Medical A");
        assertNull(team.getCoach());
    }

    // 18. MedicalStaff null
    @Test
    public void testTeamConstructor_MedicalStaffNull_KhoiTaoVanThanhCong() {
        Team team = new Team("VN", "Asia", "Coach", null);
        assertNull(team.getMedicalStaff());
    }

    // 19. thêm đúng 3 trợ lý
    @Test
    public void testAddAssistantCoaches_Them3TroLy_ThanhCong() {
        Team team = new Team("VN", "Asia", "Coach", "Medical");
        assertTrue(team.addAssistantCoach("A1"));
        assertTrue(team.addAssistantCoach("A2"));
        assertTrue(team.addAssistantCoach("A3"));
        assertEquals(3, team.getAssistantCoaches().size());
    }

    // 20. thêm >3 trợ lý-invalid
    @Test
    public void testAddAssistantCoaches_ThemQua3TroLy_ThatBai() {
        Team team = new Team("VN", "Asia", "Coach", "Med");
        team.addAssistantCoach("A1");
        team.addAssistantCoach("A2");
        team.addAssistantCoach("A3");
        boolean result = team.addAssistantCoach("A4");
        assertFalse(result);
        assertEquals(3, team.getAssistantCoaches().size());
    }

    // 21. thêm trợ lý tên rỗng-invalid
    @Test
    public void testAddAssistantCoaches_TenTrong_ThatBai() {
        Team team = new Team("VN", "Asia", "Coach", "Med");
        boolean result = team.addAssistantCoach("");
        assertFalse(result);
    }

    // 22. thêm cầu thủ-valid
    @Test
    public void testAddPlayer_ThemCauThu_ThanhCong() {
        Team team = new Team("VN", "Asia", "Coach", "Med");
        Player player = new Player("AA", 1, "Forward", "VN");
        boolean result = team.addPlayer(player);
        assertTrue(result);
        assertEquals(1, team.getPlayers().size());
    }

    // 23. thêm >22 cầu thử-invalid
    @Test
    public void testAddPlayer_ThemQua22CauThu_ThatBai() {
        Team team = new Team("VN", "Asia", "Coach", "Med");
        for (int i = 1; i <= 22; i++) {
            team.addPlayer(new Player("Player" + i, i, "Mid", "VN"));
        }
        Player extra = new Player("BB", 15, "GK", "VN");
        boolean result = team.addPlayer(extra);
        assertFalse(result);
        assertEquals(22, team.getPlayers().size());
    }

    // 24. thêm cầu thủ null-invalid
    @Test
    public void testAddPlayer_Null_ThatBai() {
        Team team = new Team("VN", "Asia", "Coach", "Med");
        boolean result = team.addPlayer(null);
        assertFalse(result);
    }

    // 25. thêm điểm
    @Test
    public void testAddPoints_CongDiem_ThanhCong() {
        Team team = new Team("VN", "Asia", "Coach", "Med");
        team.addPoints(3);
        team.addPoints(1);
        assertEquals(4, team.getPoints());
    }

    // 26. cập nhật hiệu số
    @Test
    public void testUpdateGoalsDifference_TinhDung() {
        Team team = new Team("VN", "Asia", "Coach", "Med");
        team.updateGoalDifference(4, 1); // +3
        team.updateGoalDifference(1, 2); // -1
        assertEquals(2, team.getGoalDifference());
    }

    //184. tên blank- invalid
    @Test
    public void TeamConstructor_NameWhitespace_ThrowException() {
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new Team("   ", "Asia", "Coach", "Medical");
        });
    }

    //185. region blank- invalid
    @Test
    public void TeamConstructor_RegionWhitespace_ThrowException() {
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new Team("Vietnam", "   ", "Coach", "Medical");
        });
    }

    //186. trợ lý null-invalid
    @Test
    public void AddAssistantCoaches_TenNull_ThatBai() {
        Team team = new Team("VN", "Asia", "Coach", "Med");
        boolean ok = team.addAssistantCoach(null);
        org.junit.jupiter.api.Assertions.assertFalse(ok);
        org.junit.jupiter.api.Assertions.assertEquals(0, team.getAssistantCoaches().size());
    }

    //187. nhiều lần, vẫn đúng- valid
    @Test
    public void UpdateGoalDifference_NhieuLan_DungHieuSo() {
        Team team = new Team("VN", "Asia", "Coach", "Med");
        team.updateGoalDifference(3, 1);   // +2
        team.updateGoalDifference(0, 3);   // -3  => tổng = -1
        org.junit.jupiter.api.Assertions.assertEquals(-1, team.getGoalDifference());
    }

    //188. tính tổng thẻ vàng, đỏ của đội
    @Test
    public void GetTotalCards_TinhTongTheVangDo_Dung() {
        Team team = new Team("VN", "Asia", "Coach", "Med");
        Player p1 = new Player("A", 1, "DF", "VN");
        Player p2 = new Player("B", 2, "MF", "VN");
        //p1: 2 vàng, 1 đỏ
        p1.addYellowCard(); p1.addYellowCard();
        p1.addRedCard();
        //p2: 1 vàng, 0 đỏ
        p2.addYellowCard();
        team.addPlayer(p1);
        team.addPlayer(p2);
        org.junit.jupiter.api.Assertions.assertEquals(3, team.getTotalYellowCards());
        org.junit.jupiter.api.Assertions.assertEquals(1, team.getTotalRedCards());
    }

    //189. cùng name, region
    @Test
    public void Equals_SameNameRegion_True_HashCodeEqual() {
        Team t1 = new Team("VN", "Asia", "C1", "M1");
        Team t2 = new Team("VN", "Asia", "C2", "M2"); //equal=region
        org.junit.jupiter.api.Assertions.assertTrue(t1.equals(t2));
        org.junit.jupiter.api.Assertions.assertEquals(t1.hashCode(), t2.hashCode());
    }

    //190. khác tên. region
    @Test
    public void Equals_KhacTenHoacVung_False_AndNullOtherClass() {
        Team base = new Team("VN", "Asia", "C", "M");
        Team diffName = new Team("TH", "Asia", "C", "M");
        Team diffRegion = new Team("VN", "Europe", "C", "M");
        org.junit.jupiter.api.Assertions.assertFalse(base.equals(diffName));
        org.junit.jupiter.api.Assertions.assertFalse(base.equals(diffRegion));
        org.junit.jupiter.api.Assertions.assertFalse(base.equals(null));
        org.junit.jupiter.api.Assertions.assertFalse(base.equals("not a team"));
    }
}
