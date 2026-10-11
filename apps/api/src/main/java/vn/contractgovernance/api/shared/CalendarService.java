package vn.contractgovernance.api.shared;

import org.springframework.stereotype.Service;
import java.time.*;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CalendarService {
    public static final ZoneId ZONE=ZoneId.of("Asia/Ho_Chi_Minh");
    private final Set<LocalDate> holidays;
    private final Set<Integer> workingWeekdays;
    private final LocalTime cutoff;
    public CalendarService(ContractRegistry contracts){var calendar=contracts.seed().path("calendar");var values=calendar.path("nonWorkingDates");this.holidays=java.util.stream.StreamSupport.stream(values.spliterator(),false).map(value->LocalDate.parse(value.asString())).collect(Collectors.toUnmodifiableSet());this.workingWeekdays=java.util.stream.StreamSupport.stream(calendar.path("workingWeekdays").spliterator(),false).map(value->value.asInt()).collect(Collectors.toUnmodifiableSet());this.cutoff=LocalTime.parse(calendar.path("workingDayCutoffLocal").asString());}
    public LocalDate localDate(Instant instant){return instant.atZone(ZONE).toLocalDate();}
    public Instant startOfDay(LocalDate date){return date.atStartOfDay(ZONE).toInstant();}
    public boolean businessDay(LocalDate date){return workingWeekdays.contains(date.getDayOfWeek().getValue())&&!holidays.contains(date);}
    public LocalDate addBusinessDays(LocalDate base,int count){if(count<0)throw new IllegalArgumentException("Negative business-day interval");var date=base;int remaining=count;while(remaining>0){date=date.plusDays(1);if(businessDay(date))remaining--;}return date;}
    public Instant dueByBusinessDays(Instant event,int count){return addBusinessDays(localDate(event),count).atTime(cutoff).atZone(ZONE).toInstant();}
}
