package dao;

import model.Vote;
import java.util.List;

public interface VoteDAO {
    boolean insertVote(int judgeId, int teamId, int score);
    boolean checkIfAlreadyVoted(int judgeId, int teamId);
    boolean areAllVotesCast(int hackathonId);
    List<String> getLeaderboard(int hackathonId);
    List<Vote> getVoteByTeam(int teamId);
}