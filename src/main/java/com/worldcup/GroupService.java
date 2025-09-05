package com.worldcup;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class GroupService {
    private final GroupDAO groupDao=new GroupDAO();
    private final TeamDao teamDao=new TeamDao();

    //tạo 8 gr, 4 đội/gr, trả groupId
    public List<Long> createGroups(List<Team> teams) throws SQLException {
        if (teams == null || teams.size() < 32) {
            throw new IllegalArgumentException("Can toi thieu 32 doi de chia 8 bang!");
        }

        //tạo 8 gr
        List<Long> groupIds = new ArrayList<>(8);
        for (char ch = 'A'; ch <= 'H'; ch++) {
            long gid = groupDao.insert(String.valueOf(ch));
            groupIds.add(gid);
        }

        //chia đội
        int idx = 0;
        for (int g = 0; g < 8; g++) {
            long gid = groupIds.get(g);
            for (int k = 0; k < 4; k++, idx++) {
                Team t = teams.get(idx);
                long teamId = teamDao.insert(t);
                groupDao.addTeam(gid, teamId);
            }
        }
        return groupIds;
    }
}
