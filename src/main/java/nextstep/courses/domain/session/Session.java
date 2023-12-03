package nextstep.courses.domain.session;

import nextstep.payments.domain.Payment;
import nextstep.users.domain.NsUser;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

public class Session {
    private Long id;
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
        this(0L, title, price, sessionDate, coverImage, Collections.emptyList());
    }

    public Session(final long id, final String title, final long price, final LocalDateTime startDate, final LocalDateTime endDate, final CoverImage coverImage, List<NsUser> nsUsers) {
        this(id, title, price, new SessionDate(startDate, endDate), coverImage, nsUsers);
    }

    public Session(final long id, final String title, final long price, final SessionDate sessionDate, CoverImage coverImage, List<NsUser> nsUsers) {
        validateSession(title, sessionDate);

        this.id = id;
        this.title = title;
        this.enrollment = new Enrollment(price, nsUsers);
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

    public String getTitle() {
        return this.title;
    }

    public LocalDateTime getStartDate() {
        return this.sessionDate.getStartDate();
    }

    public LocalDateTime getEndDate() {
        return this.sessionDate.getEndDate();
    }

    public int getMaxStudentLimit() {
        return this.enrollment.getMaxStudentLimit();
    }

    public void changeMaxStudentLimit(final int maxStudentLimit) {
        this.enrollment.changeMaxStudentLimit(maxStudentLimit);
    }

    public Long getId() {
        return this.id;
    }

    public CoverImage getCoverImage() {
        return this.coverImage;
    }

    public long getPrice() {
        return this.enrollment.getPrice();
    }

    public int getCurrentStudentCount() {
        return this.enrollment.getCurrentStudentCount();
    }

    private void setStatus(SessionStatus status) {
        enrollment.setStatus(status);
    }

    public boolean isNotRecruiting() {
        return !enrollment.isRecruiting();
    }

    public void ready() {
        setStatus(SessionStatus.READY);
    }

    public void recruit() {
        setStatus(SessionStatus.RECRUITING);
    }

    public void close() {
        setStatus(SessionStatus.CLOSED);
    }

    public void enroll(Payment payment, NsUser user) {
        enrollment.enroll(payment, user);
    }

    public String getSessionStatusString() {
        return this.enrollment.getSessionStatusString();
    }

    public void changeSessionStatus(final SessionStatus sessionStatus) {
        setStatus(sessionStatus);
    }

    public List<NsUser> getUsers() {
        return this.enrollment.getUsers();
    }
}
