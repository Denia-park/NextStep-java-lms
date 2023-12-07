package nextstep.courses.infrastructure;

import nextstep.courses.domain.session.*;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Repository("sessionRepository")
public class JdbcSessionRepository implements SessionRepository {
    private JdbcOperations jdbcTemplate;
    private CoverImageRepository coverImageRepository;
    private SessionUserListRepository sessionUserListRepository;
    private KeyHolder keyHolder = new GeneratedKeyHolder();
    private JdbcSessionSelectionUserListRepository sessionSelectionUserListRepository;

    public JdbcSessionRepository(JdbcOperations jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.coverImageRepository = new JdbcCoverImageRepository(jdbcTemplate);
        this.sessionUserListRepository = new JdbcSessionUserListRepository(jdbcTemplate);
        this.sessionSelectionUserListRepository = new JdbcSessionSelectionUserListRepository(jdbcTemplate);
    }

    @Override
    public int save(final Session session, final Long courseId) {
        final int updateCount = saveSession(session, courseId);
        Long sessionId = keyHolder.getKey().longValue();

        saveUsers(session.getSelectionUsers(), sessionId);

        for (final CoverImage coverImage : session.getCoverImages()) {
            coverImageRepository.save(coverImage, sessionId);
        }

        return updateCount;
    }

    private int saveSession(final Session session, final Long courseId) {
        String sql = "insert into session (title, price, start_date, end_date, session_status, max_student_limit, course_id, creator_id, created_at, recruiting_status)" +
                " values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        return jdbcTemplate.update(connection -> {
                    PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                    ps.setString(1, session.getTitle());
                    ps.setLong(2, session.getPrice());
                    ps.setTimestamp(3, toTimeStamp(session.getStartDate()));
                    ps.setTimestamp(4, toTimeStamp(session.getEndDate()));
                    ps.setString(5, session.getSessionStatusString());
                    ps.setInt(6, session.getMaxStudentLimit());
                    ps.setLong(7, courseId);
                    ps.setLong(8, 1L);
                    ps.setTimestamp(9, toTimeStamp(LocalDateTime.now()));
                    ps.setString(10, session.getRecruitingStatusString());

                    return ps;
                },
                keyHolder
        );
    }

    private void saveUsers(final TotalSelectStatusUsers users, final Long sessionId) {
        for (SelectionUser user : users.getTotalUsers()) {
            sessionSelectionUserListRepository.save(user, sessionId);
        }
    }

    private Timestamp toTimeStamp(final LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }

        return Timestamp.valueOf(localDateTime);
    }

    @Override
    public Session findById(Long id) {
        final List<SelectionUser> nsUsers = sessionSelectionUserListRepository.findAllBySessionId(id);
        final List<CoverImage> coverImages = coverImageRepository.findBySessionId(id);

        String sql = "select id, title, price, start_date, end_date, session_status, max_student_limit, creator_id, created_at from session where id = ?";
        RowMapper<Session> rowMapper = (rs, rowNum) -> {
            final Session session = new Session(
                    rs.getLong(1),
                    rs.getString(2),
                    rs.getLong(3),
                    toLocalDateTime(rs.getTimestamp(4)),
                    toLocalDateTime(rs.getTimestamp(5)),
                    null,
                    new TotalSelectStatusUsers(nsUsers),
                    rs.getLong(8),
                    toLocalDateTime(rs.getTimestamp(9))
            );

            session.changeSessionStatus(SessionStatus.of(rs.getString(6)));
            session.changeMaxStudentLimit(rs.getInt(7));
            session.setCoverImages(coverImages);

            return session;
        };

        return jdbcTemplate.queryForObject(sql, rowMapper, id);
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        }
        return timestamp.toLocalDateTime();
    }
}
