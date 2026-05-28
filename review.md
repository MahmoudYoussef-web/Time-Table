# مراجعة شاملة لمشروع Time Table Scheduler

## 📋 معلومات أساسية

| البند | القيمة |
|---|---|
| الإطار | Spring Boot 3.2.5 |
| لغة البرمجة | Java 21 |
| قاعدة البيانات | MySQL |
| أداة البناء | Maven |
| الهدف | جدولة المحاضرات الجامعية باستخدام الخوارزمية الجينية (GA) |
| ملف الجار | `timetable-scheduler-0.0.1-SNAPSHOT.jar` |

---

## 🏗️ هيكل الحزمة

```
com.example.timetable
├── TimetableSchedulerApplication.java
├── auth/
│   ├── config/   (SecurityConfig, PasswordConfig)
│   ├── dto/      (RegisterRequest, LoginRequest, AuthResponse)
│   ├── filter/   (JwtFilter)
│   ├── jwt/      (JwtUtil)
│   └── service/  (AuthService)
├── config/
│   ├── AsyncConfig.java
│   ├── DataInitializer.java
│   ├── DataLoader.java
│   └── OpenApiConfig.java
├── controller/
│   ├── academic/ (Course, Instructor, Room, Section, Semester, TimeSlot)
│   ├── admin/    (AdminController)
│   ├── auth/     (AuthController)
│   └── schedule/ (Schedule, WeeklySchedule, InstructorSchedule)
├── dto/
│   ├── request/  (8 records)
│   └── response/ (14 DTOs)
├── entity/       (17 entities + 5 enums)
├── exception/    (5 classes)
├── mapper/       (10 static mappers)
├── repository/   (14 JPA repositories)
├── scheduling/
│   ├── algorithm/      (GA core + config + strategies)
│   └── constraints/    (hard/ + soft/)
└── service/      (12 interfaces + 9 implementations)
```

---

## ✅ المميزات

### 1. بنية قوية للخوارزمية الجينية
- تطبيق كامل للخوارزمية الجينية مع أنماط `Strategy` للـ Selection و Crossover و Mutation
- دعم لـ Elitism (الحفاظ على أفضل الكروموسومات)
- إيقاف مبكر عند الوصول إلى حد اللياقة (`earlyStopThreshold`)
- حد زمني للتنفيذ (`maxExecutionMillis`) لتجنب التجميد
- دعم للـ locked entries (تثبيت محاضرات معينة)

### 2. نظام قيود متكامل
- 5 قيود صلبة (Hard Constraints): تعارض المدرسين، تعارض القاعات، سعة القاعة، تداخل الوقت، توفر المدرس
- 5 قيود لينة (Soft Constraints): المحاضرات المتتالية، الفجوات الزمنية، تكرار المادة في نفس اليوم، وقت الفراغ
- فصل كامل بين القيود الصلبة واللينة باستخدام واجهات

### 3. دورة حياة كاملة للجدول
- DRAFT → VALIDATED → PUBLISHED → LOCKED
- تحقق من عدم وجود جدول منشور آخر قبل النشر
- رفض التحقق إذا كان هناك violations صلبة

### 4. مصادقة وتفويض كاملين
- JWT مع Bearer token
- أدوار: ADMIN, INSTRUCTOR, SCHEDULER (في الـ Swagger يظهر فقط INSTRUCTOR و ADMIN)
- تحكم بالوصول على مستوى الـ endpoints باستخدام `@PreAuthorize`
- معالج 401 مخصص لإرجاع JSON بدلاً من صفحة الخطأ

### 5. توثيق Swagger/OpenAPI
- تكامل كامل مع springdoc-openapi
- دعم Bearer JWT في Swagger UI

### 6. تصدير PDF و Excel
- دعم تصدير الجدول إلى PDF باستخدام OpenPDF
- دعم تصدير الجدول إلى Excel باستخدام Apache POI

### 7. معالجة استثناءات مركزية
- `@RestControllerAdvice` مع `GlobalExceptionHandler`
- معالجة: Validation, 404, 409, 401, 403, 400, 500

### 8. استخدام Records لـ DTOs
- استخدام Java Records لـ Request/Response DTOs يقلل من boilerplate code ويضمن immutability

### 9. Seeds بيانات افتراضية
- `DataLoader` و `DataInitializer` يملآن قاعدة البيانات ببيانات اختبارية للتطوير

---

## 🔴 مشاكل حرجة **يجب إصلاحها فوراً**

### 1. 🚨 تعارض تسمية SoftConstraint — القيود اللينة لا تعمل إطلاقاً

**الملفان:** `constraints/SoftConstraint.java` و `constraints/soft/SoftConstraint.java`

توجد **واجهتان مختلفتان تماماً بنفس الاسم** في حزمتين مختلفتين:

| الواجهة | الدوال | المستخدمة في |
|---|---|---|
| `constraints.SoftConstraint` | `getName()`, `getType()`, `violations()` | `InstructorBackToBackConstraint`, `InstructorGapPreferenceConstraint`, `SameCourseSameDayConstraint` |
| `constraints.soft.SoftConstraint` | `name()`, `weight()`, `violations()` | `StudentIdleSoftConstraint`, `InstructorIdleSoftConstraint` |

**المشكلة:** `FitnessCalculator` يحقن `List<constraints.soft.SoftConstraint>`، لذلك يستقبل فقط الـ beans التي تطبق واجهة الـ subpackage. الـ 3 Constraints المطبقة بشكل صحيح (BackToBack, GapPreference, SameCourseSameDay) **يتم تجاهلها تماماً** لأنها تطبق الواجهة الخاطئة. `SoftConstraintConfig` ينشئ List منهم لكن **لا يوجد أي مستهلك لهذه الـ List**.

**النتيجة:** الـ Soft Constraints الوحيد الذي يعمل فعلياً هو `StudentIdleSoftConstraint`. باقي القيود اللينة (3 منها) هي **كود ميت لا يؤثر على النتيجة**.

**الحل:** حذف `constraints/SoftConstraint.java` وجعل كل الـ constraints تطبق `constraints.soft.SoftConstraint` (أو إضافة `weight()` إلى الـ parent interface ودمج الواجهتين).

### 2. 🚨 Bean مكرر لـ PasswordEncoder → فشل بدء التشغيل

**الملفان:** `SecurityConfig.java:69-72` و `PasswordConfig.java:12-14`

كلا الملفين يعرّفان `@Bean` لـ `PasswordEncoder` من نفس النوع. هذا يسبب `BeanDefinitionOverrideException` عند بدء التشغيل.

**الحل:** حذف فئة `PasswordConfig.java` بالكامل (أو إزالة الـ `@Bean` من `SecurityConfig`).

### 3. 🚨 N+1 Query حرج في ScheduleMapper

**الملف:** `ScheduleMapper.java:42-82`

دالة `toEntryDTO()` تصل إلى:
- `entry.getSection().getId()` ← استعلام LAZY
- `entry.getSection().getName()` ← استعلام LAZY
- `entry.getSection().getCourse().getCode()` ← استعلام LAZY (×2)
- `entry.getSection().getInstructor().getUser().getFullName()` ← استعلام LAZY (×3)
- `entry.getRoom().getRoomNumber()` ← استعلام LAZY
- `entry.getSection().getCourse().getDepartment().getName()` ← استعلام LAZY

**لكل ScheduleEntry** هذا ينتج **عدة استعلامات SQL منفصلة**. لجدول بــ 100 محاضرة، هذا يعني 600+ استعلام SQL.

**الحل:** استخدام `@EntityGraph` أو `JOIN FETCH` في الـ Repository query.

### 4. 🚨 كلمة مرور افتراضية Hardcoded - ثغرة أمنية

**الملف:** `InstructorMapper.java:22`

```java
user.setPassword("123456");
```

كل مدرس يُنشأ عبر API يأخذ كلمة مرور "123456". هذا يعني أن أي شخص يمكنه تسجيل الدخول بحساب أي مدرس إذا عرف بريده الإلكتروني.

وكذلك `DataInitializer.java` يستخدم `"admin123"`.

**الحل:** استخدام `PasswordEncoder` لتشفير كلمة المرور، وإجبار المستخدم على تعيين كلمة مرور عند الإنشاء.

### 5. 🚨 عدم وجود `@Valid` على بعض Request Bodies

**الملفات:**
- `SemesterController.create()` ← لا يوجد `@Valid`
- `TimeSlotController.create()` ← لا يوجد `@Valid`

**النتيجة:** حقل `startDate` يمكن أن يكون `null` في قاعدة البيانات رغم أنه `@Column(nullable=false)`، مما يسبب خطأ SQL 500.

### 6. 🚨 InstructorRequest بدون أي Validation

**الملف:** `InstructorRequest.java`

كل الحقول (`name`, `email`, `password`, `departmentId`) بدون `@NotBlank` أو `@NotNull`. يمكن إرسال request فارغ وسيتم إنشاء مستخدم ببريد إلكتروني فارغ.

### 7. 🚨 SemesterRequest بدون أي Validation

**الملف:** `SemesterRequest.java`

لا يوجد `@NotBlank` على `name` ولا `@NotNull` على `startDate`/`endDate`. ولا `@Future` لضمان أن التاريخ في المستقبل.

### 8. 🚨 `IllegalStateException` و `NoSuchElementException` بدون معالج عالمي

**الملف:** `GlobalExceptionHandler.java`

`ScheduleServiceImpl` يرمي `IllegalStateException` (الأسطر 54, 99, 102, 115, 124, 137, 171) و `NoSuchElementException` (الأسطر 51, 85, 149, 168, 179, 186). لكن `GlobalExceptionHandler` لا يعالج أي منهما، مما يسبب `500 Internal Server Error` للمستخدم بدلاً من رسالة مفهومة.

### 9. 🚨 التعديل على Entries في كيان Detached

**الملف:** `ScheduleServiceImpl.java:158`

```java
latest.setEntries(entries);
```

`latest` هو كيان detached (لأن `open-in-view=false`). تعديل الـ entries عليه لا يؤثر على قاعدة البيانات. لكنه يستخدم فقط للـ DTO mapping. المشكلة أن الـ LAZY loading قد يرمي `LazyInitializationException` لأن الـ session مغلقة.

---

## 🟡 مشاكل متوسطة الخطورة

### 10. تجاوز الـ duplicate entries في GeneticScheduleService

**الملف:** `GeneticScheduleService.java:54-95`

الخوارزمية الجينية تنتج genes بشكل عشوائي، ثم هذا الكود يزيل الـ duplicates. لكن **لا توجد آلية لمنع الـ duplicate أثناء التطور**. الجينات المكررة تعني أن بعض الـ sections تفقد جدولها. سعة المحاضرات (`capacity`) تقل بدون وعي.

### 11. `InstructorIdleSoftConstraint` ليس Bean

**الملف:** `InstructorIdleSoftConstraint.java`

يحتوي على `implements SoftConstraint` لكن **لا يوجد `@Component`**، لذلك لا يتم إنشاؤه كـ Spring bean ولا يُستخدم أبداً.

### 12. NoSuchElementException.orElseThrow() بدون رسالة

**الملف:** `ScheduleServiceImpl.java` (الأسطر 149, 168)

```java
.orElseThrow();
```

بدون رسالة خطأ. عند حدوث الخطأ، لا يعرف المستخدم أو المطور أي عنصر مفقود.

### 13. MySQL Connector مكرر في pom.xml

**الملف:** `pom.xml` (الأسطر 104-108 و 110-114)

نفس الـ dependency مضاف مرتين بنفس الإصدار.

### 14. Mix between Records and Lombok classes للـ DTOs

بعض DTOs هي `record` (مثل `CourseResponse`، `SectionResponse`) وبعضها Classes مع `@Data` (مثل `RoomResponse`، `ScheduleDTO`). هذا يسبب عدم الاتساق.

### 15. No PUT/PATCH endpoints

لا يوجد تعديل (Update) لأي كيان. المستخدم لا يمكنه تعديل اسم المادة أو تغيير سعة القاعة إلخ بعد الإنشاء. يجب حذف العنصر وإنشاؤه من جديد.

### 16. H2 Console مكشوفة

**الملف:** `SecurityConfig.java:38`

```java
"/h2-console/**"
```

المشروع يستخدم MySQL فعلياً، لكن routes H2 لا تزال مسموحة. إذا تم تغيير قاعدة البيانات إلى H2 للتطوير، ستكون لوحة التحكم متاحة للجميع.

### 17. InstructorAvailabilityConstraint غير آمن للخيوط المتعددة

**الملف:** `InstructorAvailabilityConstraint.java`

```java
private final Set<String> unavailableCache = new HashSet<>();
```

لأن `@Component` هو Singleton، و `preload()` يعدّل `HashSet` بدون synchronized، فإذا تم استدعاؤه من خيطين في نفس الوقت سيحدث `ConcurrentModificationException`.

### 18. Thread Safety لنظام Async

`@Async` في `ScheduleServiceImpl.generateScheduleAsync()` هو async لكن `GeneticScheduleService.generate()` لديه `@Transactional(REQUIRES_NEW)`. إذا فشلت الـ GA بعد حفظ الـ Job كـ COMPLETED، سيبقى Job في حالة COMPLETED بدون Schedule.

### 19. حفظ الـ Stack Trace في log

**الملف:** `ScheduleServiceImpl.java:73`

```java
e.printStackTrace();
```

يستخدم `e.printStackTrace()` بدلاً من `log.error("message", e)`. هذا يطبع الـ Stack Trace إلى System.err بدلاً من نظام logging، وقد يُفقد في بيئة الإنتاج.

### 20. InstructorAvailabilityConstraint لا يُستدعى preload()

**الملف:** `InstructorAvailabilityConstraint.java`

دالة `preload()` لا تُستدعى من أي مكان. الـ cache يبقى فارغاً، لذلك هذا الـ constraint لا يعمل أبداً.

---

## 🔵 مشاكل منخفضة الخطورة / اقتراحات تحسين

### 21. إظهار Stack Trace في بيئة الإنتاج

```properties
server.error.include-stacktrace=always
server.error.include-exception=true
```

يُظهر تفاصيل الـ exception للمستخدم (تسريب معلومات).

### 22. Hardcoded "LECTURE" type

**الملف:** `GeneticScheduleService.java:92`

```java
entry.setType("LECTURE");
```

نوع المحاضرة Hardcoded. لا يوجد تنوع (عملي/تمرين/محاضرة).

### 23. أسماء متغيرات سحرية (Magic Strings)

**الملف:** `WeeklyScheduleMapper.java`

يُستخدم `"L1"`, `"L2"`, `"L3"`, `"L4"` مع `startsWith()` لتحديد الفترات الزمنية. هذا غير مرن ويعتمد على تسمية الـ TimeSlot.

### 24. جميع قوائم findAll() بدون Pagination

كل `findAll()` في الخدمات قد تؤدي إلى `OutOfMemoryError` مع بيانات كثيرة (آلاف المواد، المدرسين، إلخ).

### 25. Duplicate Email عند إنشاء مدرس

لا يوجد تحقق من `existsByEmail` عند إنشاء Instructor عبر `InstructorServiceImpl.save()`.

### 26. `type` في ScheduleEntry هو String بدلاً من Enum

**الملف:** `ScheduleEntry.java`

```java
private String type;
```

استخدام String يسمح بقيم غير متوقعة. الأفضل استخدام Enum.

### 27. Unused Entities

كيانات `Exam`، `Announcement`، `ScheduleHistory` ليس لها Services أو Controllers. ربما مخطط لها مستقبلاً لكنها تبقى كود غير مستخدم حالياً.

### 28. Underscore في اسم الدالة findBySemester_Id

**الملف:** `SectionRepository.java`

```java
findBySemester_Id(Long semesterId);
```

الـ underscore في SpEL method names غير معتاد. الأفضل `findBySemesterId`.

### 29. No rate limiting على auth endpoints

`/api/auth/login` و `/api/auth/register` بدون حماية من brute force أو DDOS.

### 30. JWT Secret نص عشوائي

```properties
jwt.secret=R4nd0mVeryLongSecretKeyForHS256Algorithm1234567890
```

يجب تغييره في الإنتاج واستخدام متغير بيئة.

---

## ⚠️ ملاحظات هامة عن الأمان

1. **كلمة مرور افتراضية لكل المدرسين** ← "123456"
2. **لا تشفير لكلمات المرور** في `InstructorMapper`
3. **JWT Secret في ملف properties** ← يجب أن يكون Environment Variable
4. **لا HTTPS** ← كل البيانات (بما في ذلك كلمة المرور) تُرسل نصاً
5. **Stack traces مكشوفة للمستخدم** ← تسريب معلومات
6. **قسم `/h2-console` مفتوح** ← اختراق قاعدة البيانات عبر المتصفح
7. **Section Controller بدون `@PreAuthorize`** ← أي مستخدم مسجل يمكنه إنشاء وتعديل الـ Sections

---

## 🧪 الـ Design Patterns المستخدمة

1. **Strategy** - Selection, Crossover, Mutation Strategies
2. **Template Method** - `GeneticAlgorithm.evolve()` مع استراتيجيات قابلة للحقن
3. **Repository** - Spring Data JPA Repositories
4. **DTO** - فصل الكيانات عن الاستجابة
5. **Front Controller** - `GlobalExceptionHandler`
6. **Chain of Responsibility** - JwtFilter
7. **Facade** - `ScheduleService` كواجهة أمامية للخوارزمية وتقييم التعارضات

---

## 📊 إحصائيات الكود

| العنصر | العدد |
|---|---|
| Entity classes | 17 |
| Repositories | 14 |
| Service interfaces | 12 |
| Service implementations | 9 |
| Controllers | 11 |
| DTOs (Request) | 8 |
| DTOs (Response) | 14 |
| Mappers | 10 |
| Config classes | 8 |
| Exception classes | 5 |
| Enums | 5 |
| Hard Constraints | 5 |
| Soft Constraints | 5 (واحدة فقط فعالة!) |

---

## 🎯 ملخص التقييم النهائي

**المشروع جيد من ناحية الفكرة والهيكل العام**، لكنه يعاني من مشاكل جوهرية تمنع تشغيله بشكل صحيح:

| المستوى | العدد |
|---|---|
| 🔴 حرج (يمنع التشغيل) | 9 |
| 🟡 متوسط | 11 |
| 🔵 منخفض/تحسين | 10 |

**أهم 3 مشاكل يجب حلها فوراً:**
1. تعارض `SoftConstraint` (القيود اللينة لا تعمل)
2. تكرار `PasswordEncoder Bean` (يمنع بدء التشغيل)
3. N+1 في `ScheduleMapper` (تدهور حاد في الأداء)

---

*تمت المراجعة في 28 مايو 2026*
