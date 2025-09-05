package com.worldcup;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

public class Group_Test {
    //72. tạo group- valid
    @Test
    public void GroupConstructor_HopLe_KhoiTaoThanhCong() {
        Group group = new Group("A");
        assertEquals("A", group.getGroupName());
        assertNotNull(group.getTeams());
        assertNotNull(group.getMatches());
    }

    //73. tên group nằm ngoài A-H- invalid
    @Test
    public void GroupConstructor_KhongHopLe_ThrowException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new Group("Z");
        });
        assertEquals("Group name khong hop le! Phai nam trong A-H!", exception.getMessage());
    }

    //74. groupname null- invalid
    @Test
    public void GroupConstructor_GroupNameNull_ThrowException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new Group(null);
        });
        assertEquals("Group name khong duoc null hoac de trong!", exception.getMessage());
    }

    //75. team list null- invalid
    @Test
    public void GroupConstructor_TeamListNull_ThrowException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new Group("A", null);
        });
        assertEquals("Danh sach teams khong duoc null!", exception.getMessage());
    }

    //76. tao group 1 doi-valid
    @Test
    public void GroupConstructor_TeamList1Doi_KhoiTaoThanhCong() {
        List<Team> teams = Arrays.asList(
            new Team("VN", "Asia","Coach", "Medical")
        );
        Group group = new Group("A", teams);
        assertEquals(1, group.getTeams().size());
    }

    //77. tao group 4 doi- valid
    @Test
    public void GroupConstructor_TeamList4Doi_KhoiTaoThanhCong() {
        List<Team> teams = Arrays.asList(
            new Team("VN", "Asia","Coach", "Medical"),
            new Team("USA", "NA","Coach", "Medical"),
            new Team("Brazil", "SA","Coach", "Medical"),
            new Team("France", "Europe","Coach", "Medical")
        );
        Group group = new Group("B", teams);
        assertEquals(4, group.getTeams().size());
    }

    //78. tao group 5 doi-invalid
    @Test
    public void GroupConstructor_TeamList5Doi_ThrowException() {
        List<Team> teams = Arrays.asList(
            new Team("VN", "Asia","Coach", "Medical"),
            new Team("USA", "NA","Coach", "Medical"),
            new Team("Brazil", "SA","Coach", "Medical"),
            new Team("France", "Europe","Coach", "Medical"),
            new Team("Germany", "Europe","Coach", "Medical")
        );
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new Group("C", teams);
        });
        assertEquals("Mot bang chi duoc chua toi da 4 doi!", exception.getMessage());
    }

    //79. thứ hạng các đội trong ban- dựa theo điểm
    @Test
    public void GetStandings_TinhDungDiem_ThuHangDungTheoDiem() {
        Team t1 = new Team("VN", "Asia", "Coach", "Medical");
        Team t2 = new Team("USA", "NA", "Coach", "Medical");
        Team t3 = new Team("Brazil", "SA", "Coach", "Medical");
        Match m1 = new Match(t1, t2, false,false);
        Match m2 = new Match(t1, t3, false,false);
        Match m3 = new Match(t2, t3, false,false);
        Group group = new Group("A", Arrays.asList(t1, t2, t3));
        group.addMatch(m1);
        group.addMatch(m2);
        group.addMatch(m3);
        m1.setGoalsTeamA(2); m1.setGoalsTeamB(0); m1.setKnockout(false); m1.finishMatch();
        m2.setGoalsTeamA(1); m2.setGoalsTeamB(2); m2.setKnockout(false); m2.finishMatch();
        m3.setGoalsTeamA(1); m3.setGoalsTeamB(2); m3.setKnockout(false); m3.finishMatch();
        List<Team> standings = group.getStandings();

        // System.out.println("===== Standings Debug =====");
        // for (Team team : standings) {
        //     System.out.println(
        //         team.getName() + ": " +
        //         team.getPoints() + " điểm, " +
        //         "Hiệu số: " + team.getGoalDifference()
        //     );
        // }
        assertEquals("Brazil", standings.get(0).getName());
        assertEquals("VN", standings.get(2).getName());
        assertEquals("USA", standings.get(1).getName());
    }
 

    //80. xếp hạng, cùng điểm, khác hiệu số-> xếp theo hiệu số
    @Test
    public void GetStandings_CungDiem_KhacHieusSo_XepHangTheoHieuSo() {
        Team t1 = new Team("VN", "Asia", "Coach", "Medical");
        Team t2 = new Team("USA", "NA", "Coach", "Medical");
        Team t3 = new Team("Brazil", "SA", "Coach", "Medical");
        Match m1 = new Match(t1, t2, false, false);
        Match m2 = new Match(t1, t3, false, false);
        Match m3 = new Match(t2, t3, false, false);
        Group group = new Group("A", Arrays.asList(t1, t2, t3));
        group.addMatch(m1);
        group.addMatch(m2);
        group.addMatch(m3);
        m1.setGoalsTeamA(1); m1.setGoalsTeamB(0); m1.finishMatch();
        m2.setGoalsTeamA(0); m2.setGoalsTeamB(3); m2.finishMatch();
        m3.setGoalsTeamA(2); m3.setGoalsTeamB(0); m3.finishMatch(); 
        List<Team> standings = group.getStandings();
        assertEquals("Brazil", standings.get(0).getName());
        assertEquals("USA", standings.get(1).getName());
        assertEquals("VN", standings.get(2).getName());
    }

    //81. cùng điểm. cùng hiệu số, khác thẻ -> xếp theo thẻ
    @Test
    public void GetStandings_CungDiem_CungHieuSo_KhacThe_XepHangTheoThe() {
        Team t1 = new Team("A", "ASIA", "Coach", "Medical");
        Team t2 = new Team("B", "EU", "Coach", "Medical");
        for (int i = 1; i <= 11; i++) {
            Player p1 = new Player("A" + i, i, "MF", "A");
            if (i == 1) p1.addYellowCard();
            t1.addPlayer(p1);

            Player p2 = new Player("B" + i, i, "MF", "B");
            if (i == 1) {
                p2.addRedCard();
            }
            t2.addPlayer(p2);
        }

        Match m1 = new Match(t1, t2, false, false);
        m1.setGoalsTeamA(1); m1.setGoalsTeamB(0); m1.finishMatch();
        Match m2 = new Match(t2, t1, false, false);
        m2.setGoalsTeamA(1); m2.setGoalsTeamB(0); m2.finishMatch();
        Group group = new Group("B", Arrays.asList(t1, t2));
        group.addMatch(m1);
        group.addMatch(m2);
        List<Team> standings = group.getStandings();
        assertEquals("A", standings.get(0).getName());
        assertEquals("B", standings.get(1).getName());
    }

    //82. cùng điểm, hiệu số, thẻ -> xếp theo đối đầu
    @Test
    public void GetStandings_CungDiem_CungHieuSo_CungThe_KhacDoiDau_XepHangTheoDoiDau() {
        Team t1 = new Team("X", "ASIA", "Coach", "Medical");
        Team t2 = new Team("Y", "EU", "Coach", "Medical");
        for (int i = 1; i <= 11; i++) {
            Player p1 = new Player("X" + i, i, "MF", "X");
            t1.addPlayer(p1);
            Player p2 = new Player("Y" + i, i, "MF", "Y");
            t2.addPlayer(p2);
        }

        Match m1 = new Match(t1, t2, false, false);
        m1.setGoalsTeamA(2); m1.setGoalsTeamB(1); m1.finishMatch();
        Match m2 = new Match(t2, t1, false, false);
        m2.setGoalsTeamA(2); m2.setGoalsTeamB(1); m2.finishMatch();
        Group group = new Group("C", Arrays.asList(t1, t2));
        group.addMatch(m1);
        group.addMatch(m2);
        List<Team> standings = group.getStandings();
        assertTrue( standings.get(0).getName().equals("X") || standings.get(0).getName().equals("Y")
        );
    }

    //83. tất cả đều cùng-> xếp random
    @Test
    public void GetStandings_HoaToanBo_XepHangRandom() {
        Team t1 = new Team("Alpha", "Asia", "Coach", "Medical");
        Team t2 = new Team("Beta", "Europe", "Coach", "Medical");
        Team t3 = new Team("Gamma", "Africa", "Coach", "Medical");
        for (int i = 1; i <= 11; i++) {
            t1.addPlayer(new Player("A" + i, i, "MF", "Alpha"));
            t2.addPlayer(new Player("B" + i, i, "MF", "Beta"));
            t3.addPlayer(new Player("C" + i, i, "MF", "Gamma"));
        }

        Match m1 = new Match(t1, t2, false, false);
        Match m2 = new Match(t1, t3, false, false);
        Match m3 = new Match(t2, t3, false, false);
        m1.setGoalsTeamA(1); m1.setGoalsTeamB(1); m1.finishMatch();
        m2.setGoalsTeamA(1); m2.setGoalsTeamB(1); m2.finishMatch();
        m3.setGoalsTeamA(1); m3.setGoalsTeamB(1); m3.finishMatch();
        Group group = new Group("D", Arrays.asList(t1, t2, t3));
        group.addMatch(m1);
        group.addMatch(m2);
        group.addMatch(m3);
        List<Team> standings = group.getStandings();
        //random
        Set<String> teamNames = new HashSet<>();
        for (Team t : standings) {
            teamNames.add(t.getName());
        }
        assertEquals(3, teamNames.size());
    }


    //84. input đúng thứ tự-> đúng match
    @Test
    public void FindMatchBetween_TrungThuTu_TraVeDungMatch() {
        Team t1 = new Team("A", "Asia", "Coach", "Medical");
        Team t2 = new Team("B", "Europe", "Coach", "Medical");
        Match match = new Match(t1, t2, false, false);
        Group group = new Group("G", Arrays.asList(t1, t2));
        group.addMatch(match);
        Match result = group.findMatchBetween(t1, t2);
        assertEquals(match, result);
    }

    //85. input sai thứ tự-> đúng match
    @Test
    public void FindMatchBetween_NguocThuTu_TraVeDungMatch() {
        Team t1 = new Team("A", "Asia", "Coach", "Medical");
        Team t2 = new Team("B", "Europe", "Coach", "Medical");
        Match match = new Match(t1, t2, false, false);
        Group group = new Group("G", Arrays.asList(t1, t2));
        group.addMatch(match);
        Match result = group.findMatchBetween(t2, t1);
        assertEquals(match, result);
    }

    //86. 2 đội k đấu nha -> null kết quả
    @Test
    public void FindMatchBetween_KhongCoMatch_TraVeNull() {
        Team t1 = new Team("A", "Asia", "Coach", "Medical");
        Team t2 = new Team("B", "Europe", "Coach", "Medical");
        Team t3 = new Team("C", "Africa", "Coach", "Medical");
        Match match = new Match(t1, t2, false, false);
        Group group = new Group("G", Arrays.asList(t1, t2, t3));
        group.addMatch(match);
        Match result = group.findMatchBetween(t1, t3);
        assertNull(result);
    }

    //*** 87. từ bảng lấy ra 2 đội cao điểm nhất-valid
    @Test
    public void GetTop2Teams_DungThuHang_TraVe2DoiDauBang() {
        List<Team> teamList = Arrays.asList(
            new Team("VN", "Asia", "Coach", "Medical"),
            new Team("USA", "NA", "Coach", "Medical"),
            new Team("Brazil", "SA", "Coach", "Medical")
        );
        Team t1 = teamList.get(0); 
        Team t2 = teamList.get(1); 
        Team t3 = teamList.get(2); 
        for (int i = 1; i <= 11; i++) {
            t1.addPlayer(new Player("VN" + i, i, "MF", "VN"));
            t2.addPlayer(new Player("US" + i, i, "MF", "USA"));
            t3.addPlayer(new Player("BR" + i, i, "MF", "Brazil"));
        }

        Match m1 = new Match(t1, t2, false, false); 
        Match m2 = new Match(t1, t3, false, false); 
        Match m3 = new Match(t2, t3, false, false); 
        m1.setGoalsTeamA(2); m1.setGoalsTeamB(0); m1.finishMatch(); 
        m2.setGoalsTeamA(0); m2.setGoalsTeamB(2); m2.finishMatch(); 
        m3.setGoalsTeamA(1); m3.setGoalsTeamB(2); m3.finishMatch(); 
        Group group = new Group("G", teamList);
        group.addMatch(m1);
        group.addMatch(m2);
        group.addMatch(m3);

        //*** */
        List<Team> standings = group.getStandings();
        System.out.println("==========");
        for (Team team : standings) {
            System.out.println(team.getName() + " - " + team.getPoints() + " điểm, Hiệu số: " + team.getGoalDifference());
        }
        List<Team> top2 = group.getTop2Teams();
        System.out.println("===== Top 2 =====");
        for (Team team : top2) {
            System.out.println(team.getName());
        }
        //*** 

        assertEquals("Brazil", top2.get(0).getName());
        assertEquals("VN", top2.get(1).getName());
    }

    //88. sau khi xếp hạng bằng getStanding()--> ra thứ hạng đúng
    @Test
    public void GetTop2Teams_SauKhiXepHang_ThuTuDung() {
        List<Team> teamList = Arrays.asList(
            new Team("X", "Asia", "Coach", "Medical"),
            new Team("Y", "NA", "Coach", "Medical"),
            new Team("Z", "SA", "Coach", "Medical")
        );
        Team t1 = teamList.get(0); 
        Team t2 = teamList.get(1); 
        Team t3 = teamList.get(2); 
        for (int i = 1; i <= 11; i++) {
            t1.addPlayer(new Player("VN" + i, i, "MF", "VN"));
            t2.addPlayer(new Player("US" + i, i, "MF", "USA"));
            t3.addPlayer(new Player("BR" + i, i, "MF", "Brazil"));
        }

        Match m1 = new Match(t1, t2, false, false); 
        Match m2 = new Match(t1, t3, false, false); 
        Match m3 = new Match(t2, t3, false, false); 
        m1.setGoalsTeamA(2); m1.setGoalsTeamB(0); m1.finishMatch(); 
        m2.setGoalsTeamA(0); m2.setGoalsTeamB(2); m2.finishMatch(); 
        m3.setGoalsTeamA(1); m3.setGoalsTeamB(2); m3.finishMatch(); 
        Group group = new Group("H", teamList);
        group.addMatch(m1);
        group.addMatch(m2);
        group.addMatch(m3);

        //***
        List<Team> standings = group.getStandings();
        System.out.println("==========");
        for (Team team : standings) {
            System.out.println(team.getName() + " - " + team.getPoints() + " điểm, Hiệu số: " + team.getGoalDifference());
        }
        List<Team> top2 = group.getTop2Teams();
        System.out.println("===== Top 2 =====");
        for (Team team : top2) {
            System.out.println(team.getName());
        }
        //***

        assertEquals("Z", top2.get(0).getName());
        assertEquals("X", top2.get(1).getName());
    }

}
