package com.worldcup;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class FixtureGenerator {
    private final GroupDAO groupDao=new GroupDAO();
    private final MatchDAO matchDao=new MatchDAO();

    //6 trận/ gr
    public List<Long> generateGroupFixtures(long groupId) throws SQLException {
        List<Long> teamIds = groupDao.listTeamIds(groupId);
        if (teamIds.size() != 4) {
            throw new IllegalStateException("Group " + groupId + " phải có đúng 4 đội (hiện có " + teamIds.size() + ")");
        }

        //tao cặp đấu
        List<Long> matchIds = new ArrayList<>(6);
        for (int i = 0; i < 4; i++) {
            for (int j = i + 1; j < 4; j++) {
                long a = teamIds.get(i);
                long b = teamIds.get(j);
                long mid = matchDao.insert(groupId, a, b, false);
                matchIds.add(mid);
            }
        }
        return matchIds;
    }
}
