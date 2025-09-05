package com.worldcup;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

public class Goal_Test {
    private Match createDummyMatch(Team t1, Team t2) {
        return new Match(t1, t2);
    }

    //109. Coonstructor- valid
    @Test
    public void GoalConstructor_ValidInput_KhoiTaoThanhCong() {
        Player player = new Player("Messi", 10, "Forward", "Argentina");
        Team team = new Team("Argentina", "Conmebol", "Coach", "Medical");
        Match match = createDummyMatch(team, team);
        Goal goal = new Goal(player, team, 15, match);
        assertEquals(player, goal.getPlayer());
        assertEquals(team, goal.getTeam());
        assertEquals(15, goal.getMinute());
        assertEquals(match, goal.getMatch());
    }

    //110. player null- invalid
    @Test
    public void GoalConstructor_PlayerNull_ThrowException() {
        Team team = new Team("A", "Asia", "C", "M");
        Match match = createDummyMatch(team, team);
        assertThrows(IllegalArgumentException.class,
                () -> new Goal(null, team, 10, match));
    }

    //111. team null- invalid
    @Test
    public void GoalConstructor_TeamNull_ThrowException() {
        Player player = new Player("A", 9, "Mid", "VN");
        Match match = createDummyMatch(new Team("A", "Asia", "C", "M"), new Team("B", "Asia", "C", "M"));
        assertThrows(IllegalArgumentException.class,
                () -> new Goal(player, null, 10, match));
    }

    //112. match null- invalid
    @Test
    public void GoalConstructor_MatchNull_ThrowException() {
        Player player = new Player("A", 9, "Mid", "VN");
        Team team = new Team("VN", "Asia", "C", "M");
        assertThrows(IllegalArgumentException.class,
                () -> new Goal(player, team, 10, null));
    }

    //113. minute=0- invalid
    @Test
    public void GoalConstructor_MinuteBang0_ThrowException() {
        Player player = new Player("A", 9, "Mid", "VN");
        Team team = new Team("VN", "Asia", "C", "M");
        Match match = createDummyMatch(team, team);
        assertThrows(IllegalArgumentException.class,
                () -> new Goal(player, team, 0, match));
    }

    //114. minute= âm- invalid
    @Test
    public void GoalConstructor_MinuteAm_ThrowException() {
        Player player = new Player("A", 9, "Mid", "VN");
        Team team = new Team("VN", "Asia", "C", "M");
        Match match = createDummyMatch(team, team);
        assertThrows(IllegalArgumentException.class,
                () -> new Goal(player, team, -5, match));
    }

    //115. minute>120- invalid
    @Test
    public void GoalConstructor_MinuteLonHon120_ThrowException() {
        Player player = new Player("A", 9, "Mid", "VN");
        Team team = new Team("VN", "Asia", "C", "M");
        Match match = createDummyMatch(team, team);
        assertThrows(IllegalArgumentException.class,
                () -> new Goal(player, team, 125, match));
    }

    //116. minute=1- valid
    @Test
    public void GoalConstructor_MinuteHopLe_1_KhoiTaoThanhCong() {
        Player player = new Player("A", 9, "Mid", "VN");
        Team team = new Team("VN", "Asia", "C", "M");
        Match match = createDummyMatch(team, team);
        Goal goal = new Goal(player, team, 1, match);
        assertEquals(1, goal.getMinute());
    }

    //117. minute=120- valid
    @Test
    public void GoalConstructor_MinuteHopLe_120_KhoiTaoThanhCong() {
        Player player = new Player("A", 9, "Mid", "VN");
        Team team = new Team("VN", "Asia", "C", "M");
        Match match = createDummyMatch(team, team);
        Goal goal = new Goal(player, team, 120, match);
        assertEquals(120, goal.getMinute());
    }

    //118. getter trả về đúng
    @Test
    public void Goal_GetterTraVeDungGiaTriSauKhoiTao() {
        Player player = new Player("A", 9, "Mid", "VN");
        Team team = new Team("VN", "Asia", "C", "M");
        Match match = createDummyMatch(team, team);
        Goal goal = new Goal(player, team, 90, match);
        assertEquals(player, goal.getPlayer());
        assertEquals(team, goal.getTeam());
        assertEquals(match, goal.getMatch());
        assertEquals(90, goal.getMinute());
    }

    //119. cầu thủ k thuộc đội-> tạo đc goal
    @Test
    public void Goal_PlayerKhacTeam_ChoPhepTaoGoal() {
        Player player = new Player("Messi", 10, "Forward", "Argentina");
        Team team = new Team("Brazil", "Conmebol", "Coach", "Medical");
        Match match = createDummyMatch(team, team);
        Goal goal = new Goal(player, team, 30, match);
        assertEquals(player, goal.getPlayer());
        assertEquals(team, goal.getTeam());
    }
}
