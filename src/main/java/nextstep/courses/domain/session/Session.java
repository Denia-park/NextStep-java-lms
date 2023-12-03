package nextstep.courses.domain.session;

import nextstep.payments.domain.Payment;
import nextstep.users.domain.NsUser;
import org.springframework.util.Assert;

import java.time.LocalDateTime;

public class Session {
    private String title;
    private SessionDate sessionDate;
    private CoverImage coverImage;
    private Enrollment enrollment;

    public Session(final String title, final long price, final LocalDateTime startDate, final LocalDateTime endDate) {
        this(title, price, new SessionDate(startDate, endDate), null);
    }

    public Session(final String title, final long price, final LocalDateTime startDate, final LocalDateTime endDate, final CoverImage coverImage) {
        this(title, price, new SessionDate(startDate, endDate), coverImage);
    }

    public Session(final String title, final LocalDateTime startDate, final LocalDateTime endDate) {
        this(title, 0L, new SessionDate(startDate, endDate), null);
    }

    public Session(final String title, final long price, final SessionDate sessionDate, CoverImage coverImage) {
        validateSession(title, sessionDate);

        this.title = title;
        this.enrollment = new Enrollment(price);
        this.sessionDate = sessionDate;
        this.coverImage = validateCoverImage(coverImage);
    }

    private void validateSession(final String title, final SessionDate sessionDate) {
        Assert.hasText(title, "title cannot be blank");
        Assert.notNull(sessionDate, "session date cannot be null");
    }

    private CoverImage validateCoverImage(final CoverImage coverImage) {
        if (coverImage == null) {
            return CoverImage.defaultCoverImage();
        }

        return coverImage;
    }

    public CoverImage getCoverImage() {
        return this.coverImage;
    }

    public long getPrice2() {
        return this.enrollment.getPrice();
    }

    public int getCurrentStudentCount2() {
        return this.enrollment.getCurrentStudentCount();
    }

    private void setStatus2(SessionStatus status) {
        enrollment.setStatus(status);
    }

    public boolean isNotRecruiting2() {
        return !enrollment.isRecruiting();
    }

    public void ready() {
        setStatus2(SessionStatus.READY);
    }

    public void recruit() {
        setStatus2(SessionStatus.RECRUITING);
    }

    public void close() {
        setStatus2(SessionStatus.CLOSED);
    }

    public void enroll2(Payment payment, NsUser user) {
        enrollment.enroll(payment, user);
    }
}
