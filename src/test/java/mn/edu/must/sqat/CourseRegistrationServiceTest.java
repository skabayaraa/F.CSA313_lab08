package mn.edu.must.sqat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CourseRegistrationServiceTest {

    private static final String STUDENT_ID = "S1";
    private static final String COURSE_ID = "CSA313";
    private static final String EMAIL = "bat@must.edu.mn";

    @Mock StudentRepository repository;
    @Mock EmailSender emailSender;
    CourseRegistrationService service;

    @BeforeEach
    void setUp() {
        service = new CourseRegistrationService(repository, emailSender);
    }

    private Student student(boolean active, List<String> coursesTaken) {
        return new Student(STUDENT_ID, EMAIL, active, coursesTaken);
    }

    // A1: when + verify
    @Test
    void register_activeStudentWithPrerequisites_returnsOkSavesAndSendsEmail() {
        when(repository.findStudent(STUDENT_ID)).thenReturn(student(true, List.of("CS101")));
        when(repository.courseExists(COURSE_ID)).thenReturn(true);
        when(repository.prerequisites(COURSE_ID)).thenReturn(List.of("CS101"));

        String result = service.register(STUDENT_ID, COURSE_ID);

        assertEquals("OK", result);
        verify(repository).saveRegistration(STUDENT_ID, COURSE_ID);
        verify(emailSender).send(eq(EMAIL), anyString(), anyString());
    }

    // A2: verify(..., never())
    @Test
    void register_unknownStudent_returnsErrorNoStudentAndDoesNotSaveOrEmail() {
        when(repository.findStudent(STUDENT_ID)).thenReturn(null);

        String result = service.register(STUDENT_ID, COURSE_ID);

        assertEquals("ERROR_NO_STUDENT", result);
        verify(repository, never()).saveRegistration(anyString(), anyString());
        verify(emailSender, never()).send(anyString(), anyString(), anyString());
    }

    // A3: verifyNoInteractions
    @Test
    void register_inactiveStudent_returnsErrorInactiveAndNeverTouchesEmailSender() {
        when(repository.findStudent(STUDENT_ID)).thenReturn(student(false, List.of()));

        String result = service.register(STUDENT_ID, COURSE_ID);

        assertEquals("ERROR_INACTIVE_STUDENT", result);
        verifyNoInteractions(emailSender);
    }

    // A4: when + never
    @Test
    void register_missingPrerequisite_returnsErrorPrerequisitesAndDoesNotSave() {
        when(repository.findStudent(STUDENT_ID)).thenReturn(student(true, List.of()));
        when(repository.courseExists(COURSE_ID)).thenReturn(true);
        when(repository.prerequisites(COURSE_ID)).thenReturn(List.of("CS101"));

        String result = service.register(STUDENT_ID, COURSE_ID);

        assertEquals("ERROR_PREREQUISITES", result);
        verify(repository, never()).saveRegistration(anyString(), anyString());
    }

    // A5: doThrow(...).when(...)
    @Test
    void register_emailSenderThrows_stillReturnsOkAndSavesRegistration() {
        when(repository.findStudent(STUDENT_ID)).thenReturn(student(true, List.of("CS101")));
        when(repository.courseExists(COURSE_ID)).thenReturn(true);
        when(repository.prerequisites(COURSE_ID)).thenReturn(List.of("CS101"));
        doThrow(new RuntimeException("SMTP down"))
                .when(emailSender).send(anyString(), anyString(), anyString());

        String result = service.register(STUDENT_ID, COURSE_ID);

        assertEquals("OK", result);
        verify(repository).saveRegistration(STUDENT_ID, COURSE_ID);
    }

    // A6: assertThrows
    @Test
    void register_nullInput_throwsIllegalArgumentExceptionAndTouchesNoDependency() {
        assertThrows(IllegalArgumentException.class, () -> service.register(null, null));

        verifyNoInteractions(repository, emailSender);
    }

    // A7: ArgumentCaptor
    @Test
    void register_success_emailBodyContainsCourseCode() {
        when(repository.findStudent(STUDENT_ID)).thenReturn(student(true, List.of("CS101")));
        when(repository.courseExists(COURSE_ID)).thenReturn(true);
        when(repository.prerequisites(COURSE_ID)).thenReturn(List.of("CS101"));
        ArgumentCaptor<String> bodyCaptor = ArgumentCaptor.forClass(String.class);

        service.register(STUDENT_ID, COURSE_ID);

        verify(emailSender).send(eq(EMAIL), anyString(), bodyCaptor.capture());
        assertTrue(bodyCaptor.getValue().contains(COURSE_ID));
    }

    // A8: @ParameterizedTest + @CsvSource
    // lenient(): мөр бүр зөвхөн ӨӨРИЙН stub-уудыг ашигладаг (жишээ нь NO_STUDENT мөрөнд
    // courseExists stub огт дуудагдахгүй) тул strict stubs-ийн UnnecessaryStubbingException-оос сэргийлнэ.
    @ParameterizedTest
    @CsvSource({
            "NO_STUDENT,ERROR_NO_STUDENT",
            "INACTIVE,ERROR_INACTIVE_STUDENT",
            "MISSING_PREREQ,ERROR_PREREQUISITES"
    })
    void register_errorClass_returnsExpectedErrorCodeAndDoesNotSave(String scenario, String expected) {
        switch (scenario) {
            case "NO_STUDENT" ->
                    lenient().when(repository.findStudent(STUDENT_ID)).thenReturn(null);
            case "INACTIVE" ->
                    lenient().when(repository.findStudent(STUDENT_ID)).thenReturn(student(false, List.of()));
            case "MISSING_PREREQ" -> {
                lenient().when(repository.findStudent(STUDENT_ID)).thenReturn(student(true, List.of()));
                lenient().when(repository.courseExists(COURSE_ID)).thenReturn(true);
                lenient().when(repository.prerequisites(COURSE_ID)).thenReturn(List.of("CS101"));
            }
            default -> throw new IllegalArgumentException("Unknown scenario: " + scenario);
        }

        String result = service.register(STUDENT_ID, COURSE_ID);

        assertEquals(expected, result);
        verify(repository, never()).saveRegistration(anyString(), anyString());
        verify(emailSender, never()).send(anyString(), anyString(), anyString());
    }
}
