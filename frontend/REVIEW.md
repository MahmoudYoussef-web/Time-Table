# CampusGrid Frontend — مراجعة شاملة وتقرير الأخطاء والنواقص

> تمت المراجعة بناءً على مطابقة الـ Swagger (Backend Spring Boot 3.2.5) مع كود الـ Frontend (React + TypeScript + Vite)

---

## فهرس المشاكل

1. [أخطاء في الـ Types (عدم تطابق مع الـ Backend)](#1-types-mismatch)
2. [أخطاء في API Calls](#2-api-mismatch)
3. [صفحات مفقودة بالكامل](#3-missing-pages)
4. [صفحات وهمية (Mock Data)](#4-mock-pages)
5. [أخطاء في Routing](#5-routing-issues)
6. [أخطاء في الـ Auth Flow](#6-auth-issues)
7. [أخطاء في Forms (إرسال البيانات)](#7-form-issues)
8. [أخطاء في UI Components](#8-ui-issues)
9. [أخطاء في لوحة التنقل (Sidebar)](#9-sidebar-issues)
10. [خطة الإصلاح الكاملة (مرتبة حسب الأولوية)](#10-fix-plan)

---

## 1. أنواع (Types) لا تطابق الـ Swagger — <a id="1-types-mismatch"></a>

### 1.1 `Semester.startDate` / `endDate`
- **Backend:** `LocalDate` ← JSON → `"2026-05-30"` (بدون `T00:00:00`)
- **Frontend**: `string` ✓ — لكن `SemesterForm` و `SemestersPage` يستخدم `type="date"` ويرسل `"2026-05-30"` بشكل صحيح
- ✅ **صحيح**

### 1.2 `TimeSlot.day` — نوع الـ ENUM
- **Backend:** `DayOfWeek` enum: `SATURDAY | SUNDAY | MONDAY | TUESDAY | WEDNESDAY | THURSDAY | FRIDAY`
- **Frontend types**: نفس القيم في `types/index.ts:15` ☑️
- لكن **صفحة `TimeSlotsPage.tsx`** تستخدم `Table` مع `key: 'day'` و تعرض النص الخام `"SATURDAY"` بدون تنسيق مقروء
- ⚠️ **تحسين:** إضافة formatter للـ day

### 1.3 `JobStatus` — عدم تطابق القيم
- **Backend enum `JobStatus`:** `RUNNING`, `COMPLETED`, `FAILED`
- **Frontend `types/index.ts:14`:** `'RUNNING' | 'DONE' | 'FAILED'`
- ❌ **خطأ:** الـ Backend يرسل `"COMPLETED"` لكن الـ Frontend ينتظر `"DONE"`. كود `useScheduleJob.ts:20` يتحقق من `data.status === 'DONE'` — لن يعمل أبداً.

**حالة عدم التطابق هذه خطيرة لأنها تجعل جدولة التحقق من حالة الجدول لا تعمل أبداً.**

### 1.4 `ScheduleStatus` مفقود تماماً
- **Backend:** `ScheduleStatus.DRAFT | VALIDATED | PUBLISHED | LOCKED`
- **Frontend types:** غير موجود أصلاً
- الـ Backend يرسل `status` داخل الـ `Schedule` entity لكن الـ Frontend لا يتعامل معها إطلاقاً.
- ❌ **خطأ - مفقود**

### 1.5 `Enrollment` وأنواعه مفقودة
- **Backend:** `Enrollment` entity مع `EnrollmentRequest / EnrollmentResponse / EnrollmentResponse`
- **Frontend:** لا يوجد أي type أو API client للتسجيل (Enrollments)
- ❌ **مفقود بالكامل**

### 1.6 `Student` Requests/Responses مفقودة
- **Backend:** `StudentRequest / StudentResponse` 
- **Frontend types:** يوجد `Student` وهمي فقط في `pages/StudentsPage.tsx`
- ❌ **لا يوجد API ولا types حقيقية**

### 1.7 `UserRole` مفقود
- **Backend:** `UserRole.STUDENT | INSTRUCTOR | ADMIN`
- **Frontend:** غير موجود
- ⚠️ **مفقود — لكن قد لا نحتاجه حالياً**

### 1.8 `Schedule` Entity (الجدول المولد) — مفقود بالكامل
- **Backend:** `ScheduleDTO` بخصائص: `id, fitnessScore, hardViolations, softViolations, status, createdAt, entries, unscheduledSectionIds, semesterName, startDate, endDate`
- **Frontend:** لا يوجد نوع `ScheduleDTO` مطابق
- ❌ **مفقود بالكامل**

---

## 2. أخطاء في API Calls — <a id="2-api-mismatch"></a>

### 2.1 `schedule-controller: DELETE /api/sections/{id}` — لا يوجد
- **Frontend `sections.ts:8`:** `deleteSection` 
- **Swagger:** Section controller has `DELETE /api/sections/{id}` — response 204
- ✅ **موجود — صحيح**

### 2.2 `semester-controller: DELETE /api/semesters/{id}` — لا يوجد
- **Frontend `semesters.ts:8`:** `deleteSemester`
- **Swagger:** Semester controller **لا يوجد له DELETE endpoint**
- ❌ **خطأ:** الكود يحاول حذف semester لكن الـ API لا يدعم ذلك. سيسبب 404/405.

### 2.3 `timeslot-controller` — DELETE موجود
- **Frontend `timeslots.ts:8`:** `deleteTimeSlot`
- **Swagger:** `DELETE /api/timeslots/{id}` موجود ✓

### 2.4 `schedules: generate` — نوع الاستجابة
- **Backend:** `POST /api/schedules/generate/{semesterId}` يرجّع `UUID` (String)
- **Frontend `schedules.ts:4-7`:** `client.post<string>(...)` مع `headers: { Accept: 'text/plain' }`
- ✅ **صحيح**

### 2.5 `schedules: jobs/{jobId}` — نوع `ScheduleGenerationJob`
- **Backend:** `ScheduleGenerationJob { id: UUID, status: JobStatus, scheduleId: Long }`
- **Frontend types:** `ScheduleGenerationJob { id: string, status: JobStatus, scheduleId: number }` ✓
- لكن **JobStatus mismatch** (الرجوع للنقطة 1.3)

### 2.6 `instructor/schedule/my` — Authentication
- **Frontend `weeklySchedule.ts:7-8`:** `getMySchedule()` 
- **Backend:** `GET /api/instructor/schedule/my` — يتطلب أن المستخدم المسجل له صلاحية INSTRUCTOR
- ⚠️ **لن يعمل إذا سجل الدخول كـ ADMIN**

### 2.7 `/api/admin/ping` — غير مستخدم
- يوجد endpoint `GET /api/admin/ping` لكن لا يتم استدعاؤه من الـ Frontend
- ⚠️ يمكن استخدامه لفحص الاتصال

---

## 3. صفحات مفقودة بالكامل (بحاجة إنشاء) — <a id="3-missing-pages"></a>

### 3.1 📄 **Enrollments Page** — التسجيل
- **Backend:** `enrollment-controller` مع endpoints للتسجيل (CRUD)
- **Frontend:** لا يوجد أي صفحة أو رابط للتسجيلات
- **Required actions:** إنشاء `EnrollmentsPage.tsx` + API clients + types

### 3.2 📄 **Students Page (حقيقي)** — الطلاب
- **Backend:** `student-controller` CRUD حقيقي
- **Frontend:** `StudentsPage.tsx` الحالي **وهمي بالكامل** (mock data)
- **Required actions:** استبدال `StudentsPage.tsx` بواجهة حقيقية مع API calls

### 3.3 📄 **Weekly Schedule View (تفاصيل الجدول الأسبوعي)**
- **Frontend:** `WeeklySchedulePage.tsx` موجود لكن **الرابط `/schedules/:id/weekly` ليس في `App.tsx`**
- **الراوتر لا يحتوي على هذا المسار** — `GenerationStatus.tsx:47` يحاول التنقل إليه
- ❌ **الصفحة موجودة لكن غير معرفة في الـ Router — رابط معطل**

### 3.4 📄 **Departments View Page (Individual)**
- **Backend:** `GET /api/departments/{id}` موجود
- **Frontend:** لا توجد صفحة منفصلة لعرض department
- ⚠️ **الجدول الحالي كافٍ لكن قد نحتاج تفاصيل**

### 3.5 📄 **Course View Page (Individual)**
- **Backend:** `GET /api/courses/{id}` موجود
- **Frontend:** غير مستخدم — `CoursesPage.tsx` لا يستخدم `getCourse`

---

## 4. صفحات وهمية (Mock Data) — <a id="4-mock-pages"></a>

### 4.1 🔴 `StudentsPage.tsx` — **وهمي بالكامل**
- 10 طلاب وهميين في mock array
- لا يوجد API calls
- Search يعمل على mock data فقط
- **Required:** استبدال كامل باتصال حقيقي بالـ API

### 4.2 🔴 `DashboardPage.tsx` — **وهمي بالكامل**
- إحصائيات ثابتة (24 courses, 12 rooms, إلخ)
- `Recent Schedules` بيانات وهمية
- `Quick Actions` أزرار بدون وظائف حقيقية
- **Required:** جلب إحصائيات حقيقية من الـ API (Courses count, Rooms count, Sections count, إلخ)

### 4.3 🔴 `AnalyticsPage.tsx` — **وهمي بالكامل**
- جميع الرسوم البيانية والإحصائيات وهمية
- لا يوجد اتصال بـ API analytics
- **Required:** استخدام بيانات من `GET /api/schedules/{id}/conflicts` و `GET /api/schedules/jobs/{jobId}`

### 4.4 🔴 `ScheduleGeneratorPage.tsx` — **وهمي بالكامل**
- لا يستدعي `POST /api/schedules/generate/{semesterId}`
- ينتظر timeout 2 ثانية وهمي
- Departments/Semesters معلومات ثابتة وهمية
- معاينة mock courses
- **Required:** ربط كامل بـ API Schedule Generation + جلب semesters حقيقية

### 4.5 🔴 `SettingsPage.tsx` — **وهمي بالكامل**
- بيانات بروفايل ثابتة
- لا يوجد API لتغيير الإعدادات
- **Required:** قد يكون مقبولاً حالياً لكن يجب ربطه بـ backend عند الحاجة

---

## 5. أخطاء Routing — <a id="5-routing-issues"></a>

### 5.1 مسار `/schedules/:id/weekly` غير موجود في الـ Router
- **الملف:** `GenerationStatus.tsx:47` — `navigate(\`/schedules/${scheduleId}/weekly\`)`
- **الحقيقة:** لا يوجد Route لهذا المسار في `App.tsx`
- ❌ **خطأ:** بعد توليد الجدول، محاولة التنقل للعرض الأسبوعي ستفشل

### 5.2 مسارات مفقودة (مقترحة)
- `/instructors` — مكرر مع `/lecturers` (يحتاج توحيد)
- `/timeslots` — موجود ✅
- `/semesters` — موجود ✅
- `/sections` — موجود ✅
- `/departments` — موجود ✅
- `/weekly-schedules/:id` — مفقود
- `/instructor/my-schedule` — موجود (`/instructor/schedule` في `InstructorSchedulePage`)

---

## 6. أخطاء Auth — <a id="6-auth-issues"></a>

### 6.1 مستخدم "الطالب" — لا يعمل بشكل صحيح
- **Backend:** يدعم `UserRole.STUDENT | INSTRUCTOR | ADMIN` مع صلاحيات مختلفة
- **Frontend `AuthPage.tsx`:** يستقبل `role` أثناء التسجيل لكن **لا يرسله للـ Backend**
- `RegisterRequest` في الـ Backend: `email, password, fullName` فقط — **بدون دور**
- ❌ **حالة عدم تطابق:** يتم اختيار دور (Student/Instructor/Admin) في الواجهة لكن لا يتم إرساله لأي مكان

### 6.2 `Instructor` — بيانات ناقصة في `InstructorRequest`
- `InstructorRequest` في الـ Frontend: `{ name, email, password, departmentId }`
- `InstructorRequest` في الـ Backend: `{ name, email, password, departmentId }` ✓
- الـ Backend يقوم بإنشاء `User` + `Instructor` في نفس العملية ✅

### 6.3 `password` يُرسل مع التعديل (Update)
- `InstructorForm.tsx` يرسل `password` حتى عند التعديل
- `Zod schema:12` يجعل الباسوورد `optional().or(z.literal(''))`
- ⚠️ **مشكلة:** التعديل يرسل `password: ''` للـ Backend مما قد يسبب خطأ تحقق (Validation)

### 6.4 `AuthPage.tsx` — يرسل `university` و `department` للتسجيل لكن الـ Backend لا يقبلهما
- الحقول `university` و `department` في نموذج التسجيل ليس لها أي مقابل في `RegisterRequest`
- ❌ **هذه البيانات تُجمع لكن لا تُستخدم**

### 6.5 `LoginPage.tsx` (النسخة القديمة) — لا يزال موجوداً ومستورداً لكن غير مستخدم
- ملف `LoginPage.tsx` كامل لكنه غير مضمن في `App.tsx` Routes
- بدلاً منه يستخدم `AuthPage.tsx`
- ⚠️ يمكن حذفه للتخلص من الشوائب

---

## 7. أخطاء Forms — <a id="7-form-issues"></a>

### 7.1 `CoursesPage.tsx` — لا يعرض Departments كـ Select box
- `form.departmentId` يُدخل يدوياً (number input)
- لكن `CourseForm.tsx` الموجود يعرض Select مع قائمة Departments
- ❌ **CoursesPage.tsx لا يستخدم `CourseForm.tsx`** — يستخدم Modal مخصص

### 7.2 `LecturersPage.tsx` — Department ID يُدخل يدوياً
- `departmentId` في form هو `<input type="number">`
- لكن `InstructorForm.tsx` الموجود يعرض `<Select>` مع قائمة Departments
- ❌ **LecturersPage.tsx لا يستخدم `InstructorForm.tsx`** — يستخدم Modal مخصص

### 7.3 `CoursesPage.tsx` — تعديل Course لا يجلب `departmentId` الصحيح
- `openEdit`: `departmentId: 0` دائماً (hardcoded)
- `Course` type لا يحتوي على `departmentId` — فقط `departmentName`
- ❌ **لا يمكن تعديل الـ department الخاص بالـ Course**

### 7.4 `LecturersPage.tsx` — تعديل Instructor لا يجلب `departmentId` الصحيح
- `openEdit`: `departmentId: 0` دائماً
- ❌ **لا يمكن تعديل الـ department الخاص بالـ Instructor**

### 7.5 `SectionsPage.tsx` — `semesterId: 0` في التعديل
- `handleSubmit` للتعديل يمرر `semesterId: 0` دائماً
- ❌ **لا يمكن تعديل الـ semester للـ Section**

### 7.6 `RoomForm.tsx` — موجود لكن غير مستخدم
- `CoursesPage.tsx` و `RoomsPage.tsx` و `LecturersPage.tsx` كلها تستخدم Modals مخصصة بدلاً من Forms الجاهزة
- ❌ **تكرار في الكود — Forms موجودة لكن غير مستخدمة**

---

## 8. أخطاء UI Components — <a id="8-ui-issues"></a>

### 8.1 `ConflictBadge.tsx` — ألوان ثابتة (Hardcoded)
- يستخدم ألوان مثل `bg-red-50` و `bg-amber-50`
- ⚠️ **مشكلة في Dark Mode:** هذه الألوان لا تتغير عند التبديل إلى الوضع الداكن

### 8.2 `ScheduleCell.tsx` — ألوان ثابتة للجلسات
- `SESSION_COLORS` تستخدم `#DBEAFE` (أزرق فاتح) و `#DCFCE7` (أخضر فاتح)
- ⚠️ **مشكلة في Dark Mode**

### 8.3 `Badge.tsx` — ألوان ثابتة
- `SessionBadge` و `StatusBadge` يستخدمان ألواناً ثابتة (Tailwind colors)
- ⚠️ **مشكلة في Dark Mode**

### 8.4 `WeeklyGrid.tsx` — 6 أعمدة لكن Backend يستخدم أيام مختلفة
- `DAY_ORDER` في `lib/utils.ts`: `['SATURDAY','SUNDAY','MONDAY','TUESDAY','WEDNESDAY','THURSDAY']` ✅
- لكن عرض `WeeklyGrid` يستخدم أيام `DAY_ORDER` ويمكن أن تكون `FRIDAY` موجودة في الـ Backend لكنها ليست في العرض
- ⚠️ **تحقق:** الـ Backend يمكنه إرجاع `FRIDAY` كيوم

### 8.5 `GenerationStatus.tsx` — يستخدم `navigate` لمسار غير موجود
- تم ذكره في النقطة 5.1

---

## 9. مشاكل الـ Sidebar — <a id="9-sidebar-issues"></a>

### 9.1 مسارات مفقودة من الـ Sidebar
- `Semesters` — مهم لإدارة الفصول الدراسية
- `Sections` — مهم لربط المواد بالمدرسين
- `Time Slots` — مهم لتحديد أوقات المحاضرات
- `Departments` — مهم للأقسام الأكاديمية
- `Instructor Schedule` — للمدرّس لعرض جدوله

### 9.2 `InstructorsPage` vs `LecturersPage`
- يوجد `/lecturers` في الـ Sidebar (يستخدم `LecturersPage.tsx`)
- يوجد `/instructors` كصفحة (يستخدم `InstructorsPage.tsx`) لكن **غير موجودة في الـ Sidebar**
- ❌ **تكرار بنفس الوظيفة — يجب دمجهم**

### 9.3 `User` icon في الـ Header (AppShell)
- `User` avatar ثابت بدون صورة حقيقية
- ⚠️ يمكن تحسينه مع اسم المستخدم الفعلي

---

## 10. خطة الإصلاح الكاملة (مرتبة حسب الأولوية) — <a id="10-fix-plan"></a>

### 🔴 المرحلة الأولى — أخطاء حرجة (Critical Bugs)

| # | المشكلة | الملفات المتأثرة | الإصلاح |
|---|---------|-----------------|---------|
| 1 | `JobStatus` mismatch (`COMPLETED` vs `DONE`) | `types/index.ts:14`, `useScheduleJob.ts:20` | تغيير `'DONE'` → `'COMPLETED'` |
| 2 | مسار `/schedules/:id/weekly` غير موجود في Router | `App.tsx`, `GenerationStatus.tsx:47` | إضافة Route: `<Route path="/schedules/:id/weekly"` |
| 3 | `deleteSemester` يستدعي endpoint غير موجود | `api/semesters.ts:8` | إزالة أو استبدال بضبط الواجهة |
| 4 | `LecturersPage` و `CoursesPage` لا يستخدمان Forms الجاهزة | `pages/CoursesPage.tsx`, `pages/LecturersPage.tsx` | استخدام `CourseForm.tsx` و `InstructorForm.tsx` |
| 5 | Edit forms — `departmentId: 0` دائماً | `CoursesPage.tsx:39`, `LecturersPage.tsx:63` | إضافة `departmentId` إلى types وتمريره للتعديل |
| 6 | `password` يُرسل فارغاً في تعديل Instructor | `InstructorForm.tsx`, `pages/InstructorsPage.tsx` | عدم إرسال password إذا كان فارغاً |

### 🟡 المرحلة الثانية — بيانات وهمية تحتاج استبدال

| # | المشكلة | الإصلاح |
|---|---------|---------|
| 7 | `DashboardPage.tsx` — إحصائيات وهمية | جلب إحصائيات حقيقية: `GET /api/courses`, `/api/rooms`, `/api/instructors`, `/api/sections` وحساب العدد |
| 8 | `StudentsPage.tsx` — قائمة طلاب وهمية | استبدال بـ API حقيقي: `GET /api/students` مع إنشاء الـ API client والـ types |
| 9 | `AnalyticsPage.tsx` — بيانات وهمية | عرض بيانات من `/api/schedules/{id}/conflicts` وجلب إحصائيات مقترحة |
| 10 | `ScheduleGeneratorPage.tsx` — توليد وهمي | ربط بـ `POST /api/schedules/generate/{semesterId}` + جلب semesters حقيقية |

### 🟢 المرحلة الثالثة — صفحات مفقودة وإضافة مسارات

| # | المكون | الإجراء |
|---|--------|---------|
| 11 | **Enrollments** | إنشاء `EnrollmentsPage.tsx` + `api/enrollments.ts` + types |
| 12 | **Students** (حقيقي) | استبدال `StudentsPage.tsx` بالكامل + `api/students.ts` |
| 13 | **Schedule View** | إضافة Route `/schedules/:id/weekly` في `App.tsx` |
| 14 | **Sidebar** | إضافة روابط: Semesters, Sections, Time Slots, Departments |
| 15 | **Instructor Schedule** | إضافة `/instructor/schedule` في Routes + Sidebar |
| 16 | **Course View** (فردي) | إضافة صفحة `CourseDetailPage.tsx` (اختياري) |

### 🔵 المرحلة الرابعة — تحسينات الـ UI/UX

| # | التحسين | التفاصيل |
|---|---------|----------|
| 17 | **Dark Mode** لـ Badge/ScheduleCell | استبدال الألوان الثابتة بـ CSS variables |
| 18 | **Day formatter** لـ TimeSlots | تحويل `"SATURDAY"` → `"Saturday"` في العرض |
| 19 | **Loading states** | إضافة `Skeleton` loading للجداول |
| 20 | **Pagination** | إضافة pagination للجداول الكبيرة |
| 21 | **Toast messages** | استخدام `react-hot-toast` ثابت في كل الصفحات |
| 22 | **error boundaries** | تحسين ErrorBoundary لكل صفحة على حدة |

### 🟣 المرحلة الخامسة — تكامل كامل

| # | المهمة | الوصف |
|---|--------|-------|
| 23 | دمج `LecturersPage` ← `InstructorsPage` | اختيار الاسم الأنسب (Instructors) وتوحيد المسار |
| 24 | إزالة `LoginPage.tsx` القديم | لأنه مستبدل بـ `AuthPage.tsx` |
| 25 | `AuthPage` — إرسال الدور للـ Backend | إضافة حقل role إلى API auth (أو إنشاء مستخدم ثم ربطه بـ Instructor/Student) |
| 26 | `Schedule` types كاملة | إنشاء `ScheduleDTO` و `ScheduleResponse` |
| 27 | `Enrollment` types | إنشاء `Enrollment` و `EnrollmentRequest/Response` |

---

## ملخص سريع (Quick Stats)

| الفئة | العدد |
|-------|-------|
| 🔴 أخطاء حرجة (Critical) | 6 |
| 🟡 صفحات وهمية (Mock) | 5 |
| 🟢 صفحات مفقودة (Missing) | 3 |
| 🔵 تحسينات UI/UX | 6 |
| 🟣 تكامل وتوحيد | 5 |
| **المجموع** | **25 مشكلة** |

---

## التوصية

الـ Frontend الحالي هو **UI prototype** ممتاز من ناحية التصميم لكنه **غير متصل وظيفياً بالـ Backend** في معظم الأجزاء. الأولوية القصوى هي:

1. إصلاح `JobStatus` و `Route` — لأنها تمنع الوظيفة الأساسية (توليد الجدول)
2. استبدال `Dashboard` و `Students` بالبيانات الحقيقية
3. إضافة `Enrollments` و `Schedule View`
4. ثم تحسين UI/UX بعد التأكد من الوظائف الأساسية

الهدف النهائي هو أن يصبح كل **click** في الواجهة يُترجم إلى **API call** حقيقي، وكل **API response** ينعكس فوراً على **UI**.
