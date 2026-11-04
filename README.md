# Лаборатори №8: Mock объектууд — Mockito (F.CSA313)

- **Оюутны нэр:** Э.Энхсаруул
- **Код:** B232270145

## Орчин

```
user@LAPTOP-TKT7IHD0:~/lab08-mockito$ java -version
openjdk version "21.0.12.1" 2026-08-18
OpenJDK Runtime Environment (build 21.0.12.1+1-1-24.04.4-Ubuntu)
OpenJDK 64-Bit Server VM (build 21.0.12.1+1-1-24.04.4-Ubuntu, mixed mode, sharing)
user@LAPTOP-TKT7IHD0:~/lab08-mockito$ mvn -version
Apache Maven 3.8.7
Maven home: /usr/share/maven
Java version: 21.0.12.1, vendor: Ubuntu, runtime: /usr/lib/jvm/java-21-openjdk-amd64
Default locale: en, platform encoding: UTF-8
OS name: "linux", version: "6.6.87.2-microsoft-standard-wsl2", arch: "amd64", family: "unix"
```

## Тестийн тоо

`results/mvn-test.txt`-ийн сүүлийн "Tests run:" мөр: **Tests run: 10, Failures: 0, Errors: 0, Skipped: 0**, `BUILD SUCCESS` (7 энгийн тест + A8-ийн 3 мөр).

## A8 дээр lenient() ашигласан шалтгаан

A8-ийн мөр бүр зөвхөн өөрийн stub-уудыг ашигладаг (жишээ нь NO_STUDENT мөрөнд `courseExists` stub огт дуудагдахгүй). MockitoExtension-ий strict stubs горим ашиглагдаагүй stub-ыг `UnnecessaryStubbingException` гэж унагаадаг тул `lenient().when(...)` ашиглан мөр бүрд хэрэггүй stub-ын алдаанаас сэргийлсэн.

## Хэсэг B: Мутацийн туршилт

| № | Мутант (эвдрэл) | Унасан тестүүд | Failures / Errors | Үр дүн | Файл |
|---|-----------------|----------------|-------------------|--------|------|
| 1 | `if (!student.active()) ...` шалгалтыг комментлов | A3 `register_inactiveStudent_...`, A8 `register_errorClass_...` (INACTIVE мөр) | 2 / 0 | BUILD FAILURE | `results/mvn-test-mutant-1.txt` |
| 2 | `repository.saveRegistration(...)` дуудалтыг комментлов | A1 `register_activeStudentWithPrerequisites_...`, A5 `register_emailSenderThrows_...` | 2 / 0 | BUILD FAILURE | `results/mvn-test-mutant-2.txt` |
| 3 (засвараас өмнө) | `\|\| courseId == null` хэсгийг устгав | Ямар ч тест унаагүй (мутант амьд үлдсэн) | 0 / 0 | BUILD SUCCESS | `results/mvn-test-mutant-3-before-fix.txt` |
| 3 (A6 засварласны дараа) | Мөн адил | A6 `register_nullInput_...` | 1 / 0 | BUILD FAILURE | `results/mvn-test-mutant-3.txt` |

**Мутант 1:** идэвхгүй оюутан цааш явж `courseExists` (stub-гүй mock `false`) руу орсон тул `ERROR_INACTIVE_STUDENT`-ийн оронд `ERROR_NO_COURSE` буцсан. A8-ийн 3 мөрөөс зөвхөн INACTIVE мөр унасан.

**Мутант 2:** A1 ба A5 нь `verify(repository).saveRegistration(...)` хийдэг тул унасан. A2, A4, A8 нь алдааны замд зөвхөн `never()` шалгадаг тул унахгүй, энэ нь зөв.

**Мутант 3 ба A6-ийн засвар:** A6 нь эхэндээ `register(null, null)` дууддаг байсан. `studentId == null` ганцаараа exception шидэх тул `courseId == null` шалгалтыг устгасан ч тест ногоон хэвээр үлдсэн (цоорхой). Засвар: A6-г хоёр тусдаа `assertThrows` болгосон: `register(null, COURSE_ID)` ба `register(STUDENT_ID, null)`. Мутант 3-т `courseId == null` үед exception шидэгдэхгүй ("nothing was thrown") тул A6 унаж мутантыг барьдаг болсон. Тестийн тоо 10 хэвээр.

## Дүгнэлт

Mock нь Mockito-оор динамикаар үүсч, хэн хэзээ ямар аргументаар дуудагдсаныг өөрөө бүртгэн verify хийх боломж олгодог бол гараар бичсэн fake нь жинхэнэ ажлыг хялбаршуулан хэрэгжүүлсэн (жишээ нь санах ойн Map-тай сан) бодит класс юм. Fake нь төлөв шалгахад тохиромжтой бөгөөд олон тестэд дахин ашиглагдах боловч өөрийг нь зөв ажилладаг эсэхийг тусад нь тестлэх шаардлагатай болдог. Харин энэ лабораторийн "хадгалагдсан уу, и-мэйл явсан уу" гэх мэт харилцан үйлдлийг шалгахад mock илүү товч байсан. Mock хийх ёсгүй зүйл бол тестлэж буй классаа өөрийг нь, мөн `Student` record, `String`, `List` зэрэг энгийн утгын объектууд бөгөөд тэдгээрийг бодитоор үүсгэхэд хялбар. Mock-ийг зөвхөн өгөгдлийн сан, SMTP шиг удаан, тогтворгүй, гаднын хамаарлуудад хэрэглэнэ. Verify хэтрүүлбэл тест нь үр дүнгийн оронд хэрэгжүүлэлтийн дэлгэрэнгүйг (дуудалтын тоо, дараалал) шалгадаг болж, зөв рефакторинг хийхэд ч унадаг хэт нягт холбогдсон тест болдог. Мутант 3 нь verify-ийн тооноос гадна оролтын сонголт (`register(null, null)`) ч цоорхой үүсгэж болдгийг харуулсан. Иймд зөвхөн бизнесийн үр дүнд нөлөөтэй харилцан үйлдлийг verify хийж, нарийн ширийн зүйлийг орхих нь зөв.# Лаборатори №8: Mock объектууд — Mockito (F.CSA313)

- **Оюутны нэр:** Э.Энхсаруул
- **Код:** B232270145

## Орчин

```
PASTE_VERSIONS_HERE
```

## Тестийн тоо

`results/mvn-test.txt`-ийн сүүлийн "Tests run:" мөр: **Tests run: 10, Failures: 0, Errors: 0, Skipped: 0**, `BUILD SUCCESS` (7 энгийн тест + A8-ийн 3 мөр).

## A8 дээр lenient() ашигласан шалтгаан

A8-ийн мөр бүр зөвхөн өөрийн stub-уудыг ашигладаг (жишээ нь NO_STUDENT мөрөнд `courseExists` stub огт дуудагдахгүй). MockitoExtension-ий strict stubs горим ашиглагдаагүй stub-ыг `UnnecessaryStubbingException` гэж унагаадаг тул `lenient().when(...)` ашиглан мөр бүрд хэрэггүй stub-ын алдаанаас сэргийлсэн.

## Хэсэг B: Мутацийн туршилт

| № | Мутант (эвдрэл) | Унасан тестүүд | Failures / Errors | Үр дүн | Файл |
|---|-----------------|----------------|-------------------|--------|------|
| 1 | `if (!student.active()) ...` шалгалтыг комментлов | A3 `register_inactiveStudent_...`, A8 `register_errorClass_...` (INACTIVE мөр) | 2 / 0 | BUILD FAILURE | `results/mvn-test-mutant-1.txt` |
| 2 | `repository.saveRegistration(...)` дуудалтыг комментлов | A1 `register_activeStudentWithPrerequisites_...`, A5 `register_emailSenderThrows_...` | 2 / 0 | BUILD FAILURE | `results/mvn-test-mutant-2.txt` |
| 3 (засвараас өмнө) | `\|\| courseId == null` хэсгийг устгав | Ямар ч тест унаагүй (мутант амьд үлдсэн) | 0 / 0 | BUILD SUCCESS | `results/mvn-test-mutant-3-before-fix.txt` |
| 3 (A6 засварласны дараа) | Мөн адил | A6 `register_nullInput_...` | 1 / 0 | BUILD FAILURE | `results/mvn-test-mutant-3.txt` |

**Мутант 1:** идэвхгүй оюутан цааш явж `courseExists` (stub-гүй mock `false`) руу орсон тул `ERROR_INACTIVE_STUDENT`-ийн оронд `ERROR_NO_COURSE` буцсан. A8-ийн 3 мөрөөс зөвхөн INACTIVE мөр унасан.

**Мутант 2:** A1 ба A5 нь `verify(repository).saveRegistration(...)` хийдэг тул унасан. A2, A4, A8 нь алдааны замд зөвхөн `never()` шалгадаг тул унахгүй, энэ нь зөв.

**Мутант 3 ба A6-ийн засвар:** A6 нь эхэндээ `register(null, null)` дууддаг байсан. `studentId == null` ганцаараа exception шидэх тул `courseId == null` шалгалтыг устгасан ч тест ногоон хэвээр үлдсэн (цоорхой). Засвар: A6-г хоёр тусдаа `assertThrows` болгосон: `register(null, COURSE_ID)` ба `register(STUDENT_ID, null)`. Мутант 3-т `courseId == null` үед exception шидэгдэхгүй ("nothing was thrown") тул A6 унаж мутантыг барьдаг болсон. Тестийн тоо 10 хэвээр.

## Дүгнэлт

Mock нь Mockito-оор динамикаар үүсч, хэн хэзээ ямар аргументаар дуудагдсаныг өөрөө бүртгэн verify хийх боломж олгодог бол гараар бичсэн fake нь жинхэнэ ажлыг хялбаршуулан хэрэгжүүлсэн (жишээ нь санах ойн Map-тай сан) бодит класс юм. Fake нь төлөв шалгахад тохиромжтой бөгөөд олон тестэд дахин ашиглагдах боловч өөрийг нь зөв ажилладаг эсэхийг тусад нь тестлэх шаардлагатай болдог. Харин энэ лабораторийн "хадгалагдсан уу, и-мэйл явсан уу" гэх мэт харилцан үйлдлийг шалгахад mock илүү товч байсан. Mock хийх ёсгүй зүйл бол тестлэж буй классаа өөрийг нь, мөн `Student` record, `String`, `List` зэрэг энгийн утгын объектууд бөгөөд тэдгээрийг бодитоор үүсгэхэд хялбар. Mock-ийг зөвхөн өгөгдлийн сан, SMTP шиг удаан, тогтворгүй, гаднын хамаарлуудад хэрэглэнэ. Verify хэтрүүлбэл тест нь үр дүнгийн оронд хэрэгжүүлэлтийн дэлгэрэнгүйг (дуудалтын тоо, дараалал) шалгадаг болж, зөв рефакторинг хийхэд ч унадаг хэт нягт холбогдсон тест болдог. Мутант 3 нь verify-ийн тооноос гадна оролтын сонголт (`register(null, null)`) ч цоорхой үүсгэж болдгийг харуулсан. Иймд зөвхөн бизнесийн үр дүнд нөлөөтэй харилцан үйлдлийг verify хийж, нарийн ширийн зүйлийг орхих нь зөв.
