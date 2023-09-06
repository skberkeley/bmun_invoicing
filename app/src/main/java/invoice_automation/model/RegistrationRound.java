package invoice_automation.model;

import lombok.Getter;

import java.time.LocalDate;
import java.util.Date;

public enum RegistrationRound {
    // TODO
    BMUN_ROUND_1(
            LocalDate.of(2023, 9, 18),
            LocalDate.of(2023, 10, 9),
            LocalDate.of(2023, 10, 27)
    ),
    BMUN_ROUND_2(
        LocalDate.of(2023, 10, 7),
        LocalDate.of(2023, 11, 6),
        LocalDate.of(2023, 12, 1)
    ),
    BMUN_ROUND_3(
        LocalDate.of(2023, 11, 4),
        LocalDate.of(2023, 12, 11),
        LocalDate.of(2024, 1, 5)
    ),
    BMUN_ROUND_4(
        LocalDate.of(2023, 12, 9),
        LocalDate.of(2024, 1, 5),
        LocalDate.of(2024, 1, 26)
    ),
    FC_ROUND_1(
        LocalDate.of(2023, 8, 14),
        LocalDate.of(2023, 9, 15),
        LocalDate.of(2023, 9, 15)
    ),
    FC_ROUND_2(
            LocalDate.of(2023, 9, 16),
            LocalDate.of(2023, 10, 6),
            LocalDate.of(2023, 10, 6)
    );

    @Getter
    private final LocalDate roundStartDate;
    @Getter
    private final LocalDate schoolFeeDueDate;
    @Getter
    private final LocalDate delegateFeeDueDate;
    RegistrationRound(LocalDate roundStartDate, LocalDate schoolFeeDueDate, LocalDate delegateFeeDueDate) {
        this.roundStartDate = roundStartDate;
        this.schoolFeeDueDate = schoolFeeDueDate;
        this.delegateFeeDueDate = delegateFeeDueDate;
    }
}
