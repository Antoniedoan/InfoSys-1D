package profchoper.professor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@SuppressWarnings("unchecked")
public class ProfessorRepository {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public ProfessorRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Professor> findAll() {
        String selectSQL = "SELECT * FROM professors ORDER BY alias";

        return jdbcTemplate.query(selectSQL, new ProfessorRowMapper());
    }

    public List<Professor> findByCourseId(int courseId) {
        String selectSQL = "SELECT * FROM professors WHERE course_id = ? ORDER BY alias";

        return jdbcTemplate.query(selectSQL, new Object[]{courseId}, new ProfessorRowMapper());
    }

    public Professor findByAlias(String alias) {
        String selectSQL = "SELECT * FROM professors WHERE alias = ?";

        return (Professor) jdbcTemplate.queryForObject(selectSQL, new Object[]{alias},
                new BeanPropertyRowMapper(Professor.class));
    }
}
