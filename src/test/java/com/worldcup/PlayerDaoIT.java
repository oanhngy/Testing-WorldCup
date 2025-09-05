package com.worldcup;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;


public class PlayerDaoIT extends BaseIT {
    private PlayerDAO pdao;

    @BeforeEach
    void setup() {
        pdao=new PlayerDAO();
    }

    //insert 1 cầu thủ (fk tạo team trước)
    //xác thực mapping Player(name, num, position, teamName), truy vấn theo đội
    @Test
    void playerCrudSingle() throws Exception {
        teamDao.insert(new Team("Vietnam", "Asia", "VNC", "MedVN"));    
        Player p=new Player("AAA",7,"FW","Vietnam");
        p.addYellowCard();
        p.addYellowCard();
        p.addRedCard();

        long pid=pdao.insert(p);
        assertTrue(pid>0, "Player ID phai >0 sau insert.");

        //tìm theo id
        Player byId = pdao.findById(pid).orElseThrow();
        assertEquals("AAA", byId.getName());
        assertEquals(7, byId.getNumber());
        assertEquals("FW", byId.getPosition());
        assertEquals("Vietnam", byId.getTeamName());
        assertEquals(2, byId.getYellowCards());
        assertEquals(1, byId.getRedCards());

        //tìm theo teamName
        List<Player> inTeam = pdao.findByTeamName("Vietnam");
        assertEquals(1, inTeam.size());
        assertEquals("AAA", inTeam.get(0).getName());
    }

    //insert nhiều cầu thủ/ đội
    @ParameterizedTest(name = "Insert {0} (#{1}) cho đội {2}")
    @CsvSource({"AA,1,Brazil", "BB,4,Brazil", "CC,7,Brazil"
    })
    void insertManyAndQuery(String name, int number, String teamName) throws Exception {
        teamDao.insert(new Team(teamName, "SA", "Coach", "Med"));

        long pid = pdao.insert(new Player(name, number, "ANY", teamName));
        assertTrue(pid > 0);

        var players = pdao.findByTeamName(teamName);
        assertTrue(players.stream().anyMatch(pl -> pl.getName().equals(name)));
    }

    //cập nhật thẻ, xóa theo teamName
    //check updateCards, deleteAllByTeamName
    @Test
    void updateCardsAndDelete() throws Exception {
        teamDao.insert(new Team("Japan", "AS", "XXX", "Med"));

        long pid = pdao.insert(new Player("Y", 20, "MF", "Japan"));
        assertEquals(1, pdao.updateCards(pid, 1, 1));

        Player after = pdao.findById(pid).orElseThrow();
        assertEquals(1, after.getYellowCards());
        assertEquals(1, after.getRedCards());

        int deleted = pdao.deleteAllByTeamName("Japan");
        assertTrue(deleted >= 1);
        assertTrue(pdao.findById(pid).isEmpty());
    }
}


//chạy hết *IT
//mvn verify

//chỉ chạy 1
// mvn -Dit.test=TeamDaoIT verify
