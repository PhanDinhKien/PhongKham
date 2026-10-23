# HỌC VIỆN CÔNG NGHỆ BƯU CHÍNH VIỄN THÔNG

## KHOA CÔNG NGHỆ THÔNG TIN

-------------------- \* --------------------

# BÀI TẬP THỰC HÀNH LẬP TRÌNH WEB

**Đề tài:** Xây dựng hệ thống quản trị phòng khám – quản lý hợp đồng khám sức khỏe, định mức dịch vụ, chính sách giảm giá và phiếu thu
_(Mirai Healthcare – Spring Boot, Thymeleaf, SQL Server)_

**Sinh viên thực hiện:** Phan Đình Kiên
**Mã số sinh viên:** K24DTCN452
**Lớp:** `[ĐIỀN LỚP]`
**Giáo viên hướng dẫn:** `[ĐIỀN TÊN GIÁO VIÊN]`

HÀ NỘI, 2026

---

## PHÂN CÔNG NHIỆM VỤ THÀNH VIÊN

| STT | Họ và tên      | Mã số      | Vai trò           | Nhiệm vụ chi tiết                                                                                                                                                                                  |
| --- | -------------- | ---------- | ----------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 1   | Phan Đình Kiên | K24DTCN452 | Thực hiện toàn bộ | Phân tích yêu cầu, thiết kế cơ sở dữ liệu (Liquibase), xây dựng backend (Spring Boot), giao diện quản trị (Thymeleaf, HTML/CSS/JavaScript), xác thực – phân quyền (JWT), dữ liệu mẫu, viết báo cáo |

Dự án do một mình sinh viên thực hiện, không có thành viên khác.

---

## MỤC LỤC

- I. MỞ ĐẦU
  - 1.1. Giới thiệu về nội dung chính đề tài
    - 1.1.1. Lý do chọn đề tài
    - 1.1.2. Mục tiêu hệ thống
    - 1.1.3. Phạm vi và đối tượng sử dụng
    - 1.1.4. Chức năng chính
    - 1.1.5. Điểm nổi bật của sản phẩm
  - 1.2. Công nghệ – kỹ thuật sử dụng
- II. PHÂN TÍCH THIẾT KẾ
  - 2.1. Mô tả chức năng
    - 2.1.1. Tác nhân và sơ đồ use case
    - 2.1.2. Danh sách chức năng
    - 2.1.3. Đặc tả chi tiết các chức năng
    - 2.1.4. Luồng bán dịch vụ và lập phiếu thu
  - 2.2. Thiết kế CSDL
    - 2.2.1. Danh sách bảng
    - 2.2.2. Chi tiết các bảng chính
  - 2.3. Giao diện chính
    - 2.3.1. Giao diện đăng nhập
    - 2.3.2. Giao diện quản trị
  - 2.4. Thiết kế xác thực và phân quyền
    - 2.4.1. Kiến trúc tổng thể
    - 2.4.2. Cơ chế access token và refresh token
    - 2.4.3. Quy tắc bảo vệ tài khoản
    - 2.4.4. Phân quyền theo vai trò
- III. TRIỂN KHAI
  - 3.1. Cấu trúc project
    - 3.1.1. Cấu hình và bảo mật
  - 3.2. Các chức năng đã làm
  - 3.3. Code tiêu biểu
  - 3.4. Kiểm thử
  - 3.5. Hướng dẫn cài đặt và chạy
- IV. KẾT QUẢ & ĐÁNH GIÁ
  - 4.1. Kết quả đạt được
  - 4.2. Hạn chế
- V. KẾT LUẬN
- VI. TÀI LIỆU THAM KHẢO

> **Ghi chú chèn ảnh:** Mọi vị trí cần ảnh được đánh dấu dạng `📷 [CHÈN ẢNH – Hình x.x]` kèm URL cần chụp. Địa chỉ gốc dùng cổng mặc định của Spring Boot là `http://localhost:8080` (dự án không cấu hình cổng riêng). Cần đăng nhập trước khi chụp các trang quản trị.

---

# I. MỞ ĐẦU

## 1.1. Giới thiệu về nội dung chính đề tài

### 1.1.1. Lý do chọn đề tài

Hệ thống PhongKham (tên hiển thị trên giao diện: **Mirai Healthcare Admin**) là ứng dụng web quản trị cho phòng khám có hoạt động khám sức khỏe theo **hợp đồng với công ty/doanh nghiệp**. Trong mô hình này, một hợp đồng gắn với nhiều người bệnh, nhiều địa điểm khám, nhiều gói khám, và có định mức số lượng dịch vụ cùng chính sách giảm giá riêng. Hệ thống gom các nghiệp vụ đó vào một ứng dụng để quản lý tập trung.

### 1.1.2. Mục tiêu hệ thống

- Quản lý danh mục dữ liệu nền: dịch vụ y tế, địa điểm khám và ca làm việc, hồ sơ người bệnh.
- Quản lý hợp đồng khám với công ty, gồm người bệnh, địa điểm khám và gói khám của từng hợp đồng.
- Thiết lập định mức dịch vụ và chính sách giảm giá theo bậc số lượng.
- Lập phiếu thu cho người bệnh theo hợp đồng và theo dõi danh sách phiếu thu.
- Quản lý tài khoản, vai trò, quyền; đăng nhập an toàn bằng JWT.

### 1.1.3. Phạm vi và đối tượng sử dụng

- **Đối tượng sử dụng:** người quản trị hệ thống. Các API nghiệp vụ hiện chỉ cho phép tài khoản có vai trò `ADMIN` (xem mục 2.4.4).
- **Phạm vi:** giao diện quản trị (web, Thymeleaf) và REST API phía sau. Hệ thống không có giao diện dành cho người bệnh.
- Các module `medicalrecord` (hồ sơ bệnh án) và `medicalservice` hiện chỉ có thư mục rỗng, chưa có chức năng.

### 1.1.4. Chức năng chính

| Nhóm                  | Chức năng                                                                                 |
| --------------------- | ----------------------------------------------------------------------------------------- |
| Quản trị hệ thống     | Tài khoản; Vai trò & Quyền                                                                |
| Quản lý khám bệnh     | Hồ sơ người bệnh                                                                          |
| Kinh doanh & Hợp đồng | Quản lý hợp đồng (chi tiết hợp đồng: hồ sơ khám, địa điểm, gói khám); Danh sách phiếu thu |
| Thiết lập chung       | Địa điểm khám (kèm ca làm việc); Danh mục dịch vụ; Định mức dịch vụ; Chính sách giảm giá  |
| Xác thực              | Đăng nhập, đăng xuất, làm mới token                                                       |

_(Tên các nhóm lấy đúng từ menu bên trái của giao diện.)_

### 1.1.5. Điểm nổi bật của sản phẩm

- **Chi tiết hợp đồng** gom 3 tab (hồ sơ khám, địa điểm khám, gói khám) và có thao tác bán dịch vụ, lập phiếu thu ngay trong trang.
- **Chính sách giảm giá theo bậc số lượng**: hỗ trợ giảm theo phần trăm hoặc số tiền, hai cách tính bậc (bậc lớn nhất cho toàn bộ hoặc lũy tiến), mức giảm tối đa trên mỗi dịch vụ, áp dụng cho một hoặc nhiều hợp đồng.
- **Định mức dịch vụ** theo địa điểm, hợp đồng và ca khám (ca 1, ca 2).
- **Bảo mật đăng nhập:** mật khẩu băm BCrypt, khóa tài khoản khi đăng nhập sai nhiều lần, refresh token có xoay vòng và chỉ lưu bản băm trong cơ sở dữ liệu.
- **Quản lý thay đổi CSDL bằng Liquibase** (19 changelog) và **tự tạo dữ liệu mẫu** khi khởi động (seeder).
- **Xóa mềm** với người bệnh, dịch vụ, địa điểm, hợp đồng, gói khám, chính sách giảm giá (có các cột `is_deleted`, `deleted_at`, `deleted_by`).

## 1.2. Công nghệ – kỹ thuật sử dụng

| Thành phần         | Công nghệ (theo `build.gradle`)                                                     |
| ------------------ | ----------------------------------------------------------------------------------- |
| Ngôn ngữ / Runtime | Java 17                                                                             |
| Framework          | Spring Boot 4.1.1 (`starter-webmvc`)                                                |
| Giao diện          | Thymeleaf, HTML, CSS, JavaScript thuần (`fetch` gọi API); biểu tượng Phosphor Icons |
| Truy cập dữ liệu   | Spring Data JPA (Hibernate), QueryDSL (`io.github.openfeign.querydsl` 7.7)          |
| Cơ sở dữ liệu      | Microsoft SQL Server 2022 (chạy bằng Docker)                                        |
| Quản lý schema     | Liquibase (YAML) – `ddl-auto: validate`                                             |
| Bảo mật            | Spring Security, JWT (jjwt 0.12.6), BCrypt (`spring-security-crypto`)               |
| Kiểm tra dữ liệu   | `spring-boot-starter-validation`                                                    |
| Build              | Gradle (Gradle Wrapper)                                                             |
| Môi trường         | Docker Compose (SQL Server), file `.env` cho biến môi trường                        |

---

# II. PHÂN TÍCH THIẾT KẾ

## 2.1. Mô tả chức năng

### 2.1.1. Tác nhân và sơ đồ use case

**Tác nhân:**

| Tác nhân                | Mô tả                                                                                                                                                                          |
| ----------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| Quản trị viên (`ADMIN`) | Đăng nhập và sử dụng toàn bộ chức năng quản trị.                                                                                                                               |
| Các vai trò khác        | `BAC_SI_KET_LUAN`, `NV_HO_SO`, `KY_THUAT_VIEN`, `BAC_SI` được tạo sẵn khi khởi động. Hiện các API nghiệp vụ chỉ cho phép `ADMIN`, nên các vai trò này chưa có chức năng riêng. |

📷 **[CHÈN ẢNH – Hình 2.1: Sơ đồ use case]** Tự vẽ sơ đồ use case cho tác nhân Quản trị viên, gồm các use case: Đăng nhập/Đăng xuất; Quản lý tài khoản; Quản lý vai trò & quyền; Quản lý hồ sơ người bệnh; Quản lý hợp đồng; Quản lý địa điểm/hồ sơ khám/gói khám của hợp đồng; Lập phiếu thu; Xem danh sách phiếu thu; Quản lý địa điểm khám; Quản lý danh mục dịch vụ; Quản lý định mức dịch vụ; Quản lý chính sách giảm giá. (Không có URL, đây là sơ đồ vẽ tay hoặc bằng công cụ vẽ.)

### 2.1.2. Danh sách chức năng

| STT | Chức năng               | Trang giao diện                                                             | Mô tả ngắn                                                                                |
| --- | ----------------------- | --------------------------------------------------------------------------- | ----------------------------------------------------------------------------------------- |
| 1   | Đăng nhập / Đăng xuất   | `/login`, `/logout`                                                         | Đăng nhập bằng email và mật khẩu                                                          |
| 2   | Quản lý tài khoản       | `/admin/users`                                                              | Tìm kiếm, thêm, sửa, khóa/mở khóa, xóa tài khoản; gán vai trò                             |
| 3   | Quản lý vai trò & quyền | `/admin/roles`, `/admin/roles/create`, `/admin/roles/{id}/edit`             | Danh sách, thêm, sửa, khóa, xóa vai trò; gán quyền                                        |
| 4   | Hồ sơ người bệnh        | `/admin/patients`                                                           | Tìm kiếm, lọc giới tính, thêm, sửa, xóa mềm                                               |
| 5   | Quản lý hợp đồng        | `/admin/contracts`                                                          | Tìm kiếm, lọc trạng thái, thêm, sửa, xóa                                                  |
| 6   | Chi tiết hợp đồng       | `/admin/contracts/{id}`                                                     | Hồ sơ khám, địa điểm khám, gói khám; bán dịch vụ; lập phiếu thu                           |
| 7   | Danh sách phiếu thu     | `/admin/receipts`                                                           | Tìm kiếm, lọc, xem chi tiết, đổi trạng thái đồng bộ, xuất CSV                             |
| 8   | Địa điểm khám           | `/admin/locations`                                                          | Thêm, sửa, xóa mềm, bật/tắt; cấu hình ca; gán hợp đồng cho địa điểm                       |
| 9   | Danh mục dịch vụ        | `/admin/services`                                                           | Thêm, sửa, xóa mềm, bật/tắt dịch vụ y tế                                                  |
| 10  | Định mức dịch vụ        | `/admin/service-quotas`                                                     | Thiết lập số lượng áp dụng theo dịch vụ, địa điểm, hợp đồng, ca; đồng bộ từ HIS (giả lập) |
| 11  | Chính sách giảm giá     | `/admin/discounts`, `/admin/discounts/create`, `/admin/discounts/{id}/edit` | Thêm, sửa, bật/tắt, xóa chính sách và bảng bậc chiết khấu                                 |

_(Các URL trên lấy từ `AdminViewController`.)_

### 2.1.3. Đặc tả chi tiết các chức năng

**a) Đăng nhập**

- Đầu vào: email và mật khẩu (form `/login`).
- Xử lý: kiểm tra tài khoản tồn tại, còn hoạt động, không bị khóa tạm thời, mật khẩu đúng (BCrypt). Thành công thì cấp access token và refresh token, lưu vào cookie `accessToken` và `refreshToken` (HttpOnly), chuyển đến `/admin/dashboard`.
- Thất bại: hiển thị thông báo lỗi trên trang đăng nhập (ví dụ "Email không đúng", "Sai mật khẩu", "Tài khoản của bạn đang bị khóa").

**b) Quản lý tài khoản**

- Danh sách có tìm kiếm theo mã/tên nhân viên, lọc theo vai trò và trạng thái (Hoạt động/Đã khóa), phân trang (10/20/50/100).
- Cột: STT, Mã nhân viên, Tên nhân viên, Tên đăng nhập, Vai trò, Trạng thái, Tác vụ.
- Thêm/sửa: họ và tên, nhân sự (mã nhân viên, gắn với tài khoản HIS), tên đăng nhập (email), số điện thoại, mật khẩu, vai trò hệ thống.
- Khóa/mở khóa tài khoản. Khi xóa tài khoản, hệ thống xóa liên kết vai trò, xóa phiên đăng nhập (refresh token) rồi xóa tài khoản.
- Email không được trùng.

**c) Quản lý vai trò & quyền**

- Danh sách vai trò: Mã, Tên, Mô tả, Số tài khoản, Quyền hạn, Trạng thái; lọc Hoạt động/Đã khóa.
- Màn hình thêm/sửa riêng: tên vai trò, mã vai trò, mô tả, danh sách quyền (có ô tìm theo tên hoặc mã quyền).
- Ràng buộc: mã vai trò không trùng; **không thể khóa, vô hiệu hóa hoặc xóa vai trò `ADMIN`**; không xóa vai trò đang có tài khoản sử dụng.
- Danh sách quyền hạn (29 quyền, 7 nhóm): Người dùng, Vai trò (gồm "Phân quyền cho vai trò"), Dịch vụ y tế, Địa điểm khám, Hồ sơ người bệnh, Hợp đồng, Chính sách giảm giá; mỗi nhóm có các quyền xem, tạo, sửa, xóa.

**d) Hồ sơ người bệnh**

- Danh sách: tìm theo mã BN, họ tên, số điện thoại, địa chỉ; lọc giới tính (Nam/Nữ/Khác); phân trang.
- Cột: STT, Mã người bệnh, Họ và tên, Năm sinh, Giới tính, Số điện thoại, Địa chỉ, Ghi chú (bệnh nền), Người sửa gần nhất, Tác vụ.
- Mã người bệnh tự sinh theo dạng `BN00001`, `BN00002`…, không được trùng. Xóa là xóa mềm.

**e) Quản lý hợp đồng**

- Danh sách: tìm theo mã HĐ, tên hợp đồng, mã hoặc tên công ty; lọc trạng thái (Hiệu lực, Chưa hiệu lực, Hết hiệu lực, Đã hủy).
- Thêm/sửa: mã hợp đồng (tự sinh dạng `HD00001`, `HD00002`…), trạng thái, tên hợp đồng, mã công ty, tên công ty.
- Mã hợp đồng không được trùng. Xóa là xóa mềm.

**f) Chi tiết hợp đồng** (`/admin/contracts/{id}`) gồm 3 tab:

1. **Hồ sơ khám:** danh sách người bệnh trong hợp đồng (STT, Mã NB, Mã hồ sơ, Bệnh nhân, Bác sĩ, Ngày khám, Ngày cập nhật, Trạng thái, Đồng bộ, Tác vụ); lọc theo trạng thái hồ sơ (Mới, Chờ duyệt, Đang duyệt, Đã duyệt). Thêm người bệnh vào hợp đồng với mã hồ sơ khám, bác sĩ phụ trách, ngày khám, trạng thái hồ sơ. Một người bệnh không được thêm hai lần vào cùng hợp đồng.
2. **Địa điểm khám:** danh sách địa điểm được gán (Mã, Tên, Địa chỉ, Trạng thái, Ngày gán); gán/gỡ địa điểm. Một địa điểm không được gán hai lần vào cùng hợp đồng.
3. **Gói khám:** danh sách gói (Mã gói, Tên gói khám, Số dịch vụ, Ngày tạo, Người tạo); thêm/sửa/xóa gói (trạng thái Hiệu lực hoặc Hết hiệu lực); gán dịch vụ cho gói bằng cách tích chọn từ danh mục (có tìm kiếm và lọc theo phân loại). Mã gói không trùng trong cùng hợp đồng.

**g) Phiếu thu**

- Lập từ trang chi tiết hợp đồng (xem mục 2.1.4). Danh sách phiếu thu: tìm theo mã NB, mã hồ sơ, tên NB; lọc theo công ty, người thu, hình thức thanh toán (Chuyển khoản, Tiền mặt, Quẹt thẻ), khoảng thời gian (Hôm nay, Tuần này, Tháng này, Năm nay, Tất cả), trạng thái đồng bộ (Đã đồng bộ, Chưa đồng bộ, Lỗi đồng bộ).
- Cột: STT, Mã phiếu thu, Mã NB, Mã hồ sơ, Tên người bệnh, Giới tính, Năm sinh, Công ty, Tổng SL dịch vụ, Tiền đã thu, Người thu, Thời gian, Tác vụ.
- Xem chi tiết phiếu (khách thanh toán, tiền thừa hoàn lại, thu ngân, ghi chú), đổi qua lại trạng thái đồng bộ, xuất danh sách ra file CSV (hỗ trợ tiếng Việt trong Excel).
- Mã phiếu có dạng `yyMMdd` + số thứ tự 5 chữ số. Hệ thống phân loại dịch vụ thành nhóm xét nghiệm và nhóm chẩn đoán hình ảnh, lưu toàn bộ dịch vụ đã chọn dạng JSON.

**h) Địa điểm khám**

- Danh sách: tìm theo mã/tên địa điểm; cột Mã địa điểm, Tên địa điểm, Ca 1, Ca 2, Tác vụ; phân trang.
- Thêm/sửa: mã (tự sinh), tên, ca làm việc (số ca, giờ bắt đầu, giờ kết thúc). Mặc định tạo 2 ca.
- Gán hợp đồng cho địa điểm (kèm cấu hình ca làm việc cho hợp đồng đó), bật/tắt hoặc gỡ liên kết hợp đồng. Một hợp đồng không được gán hai lần vào cùng địa điểm.

**i) Danh mục dịch vụ y tế**

- Danh sách: tìm theo mã/tên; lọc loại dịch vụ và trạng thái (Đang áp dụng, Tạm dừng); cột Mã dịch vụ, Tên dịch vụ, Loại dịch vụ, Đơn giá, Mô tả, Người cập nhật, Trạng thái, Tác vụ.
- Loại dịch vụ: Xét nghiệm, Chẩn đoán hình ảnh, Khám chuyên khoa, Thủ thuật, Khác. Mã dịch vụ không trùng. Xóa là xóa mềm.

**j) Định mức dịch vụ**

- Danh sách dịch vụ kèm số lượng áp dụng; mở rộng từng dòng để xem các định mức của dịch vụ đó. Lọc theo loại dịch vụ.
- Thiết lập định mức: chọn dịch vụ, địa điểm khám, hợp đồng áp dụng (hoặc để trống để áp dụng chung), thời gian áp dụng; số lượng tối đa, số lượng ca 1 và ca 2.
- Địa điểm phải có cấu hình ca và đang hoạt động. Không cho trùng định mức của cùng dịch vụ, địa điểm, hợp đồng. Có thao tác bật/tắt, xóa và **đồng bộ từ HIS (giả lập – mock)**.

**k) Chính sách giảm giá**

- Danh sách: tìm theo mã/tên chính sách, tên công ty/hợp đồng; lọc hình thức giảm giá (Phần trăm, Số tiền) và trạng thái (Đang áp dụng, Ngừng áp dụng); cột Mã chính sách, Tên chính sách, Loại áp dụng, Giá trị CK, Thời gian áp dụng, Trạng thái áp dụng, Tác vụ.
- Form thêm/sửa: mã (tự sinh, ví dụ `CK-023`), tên, loại áp dụng, cách xác định mức chiết khấu, hình thức (Phần trăm hoặc Số tiền), **bảng bậc chiết khấu** (bậc, từ số lượng, đến số lượng, mức chiết khấu), phạm vi áp dụng mức bậc, chiết khấu tối đa trên mỗi dịch vụ, từ ngày – đến ngày, chọn một hoặc nhiều hợp đồng áp dụng.
- Phạm vi áp dụng bậc: "Áp dụng bậc lớn nhất cho toàn bộ dịch vụ" hoặc "Tính lũy tiến theo từng bậc".

### 2.1.4. Luồng bán dịch vụ và lập phiếu thu

Luồng thực hiện trong trang chi tiết hợp đồng, tab hồ sơ khám:

1. Chọn một bệnh nhân trong hợp đồng để mở cửa sổ bán dịch vụ.
2. Chọn gói khám trong hợp đồng, điểm khám (chỉ gồm các địa điểm đã gán cho hợp đồng) và ca khám (lấy từ cấu hình ca của địa điểm). Dịch vụ thuộc gói khám đã chọn được tích sẵn.
3. Chọn thêm hoặc bỏ dịch vụ trong bảng (Dịch vụ, SL còn lại, Đơn giá), hệ thống tính tạm tính theo đơn giá gốc.
4. Mở phiếu thu: hệ thống tìm chính sách giảm giá đang áp dụng của hợp đồng, xác định bậc theo tổng số dịch vụ đã chọn rồi tính chiết khấu:
   - Hình thức **Phần trăm**: chiết khấu = tạm tính × mức bậc / 100; nếu có "chiết khấu tối đa / dịch vụ" thì giới hạn bằng mức đó × số dịch vụ.
   - Hình thức **Số tiền**: chiết khấu = mức bậc, tối đa bằng tạm tính.
   - Thành tiền = tạm tính − chiết khấu (không âm).
5. Nhập khách thanh toán, tiền thừa hoàn lại, thu ngân, ghi chú, hình thức thanh toán, rồi lưu. Phiếu thu được ghi qua `POST /api/receipts`. Mặc định hình thức thanh toán là chuyển khoản; phiếu mới có trạng thái in "Đã in" và trạng thái đồng bộ "Đã đồng bộ".

📷 **[CHÈN ẢNH – Hình 2.2: Sơ đồ luồng bán dịch vụ và lập phiếu thu]** Tự vẽ sơ đồ theo 5 bước trên.

---

## 2.2. Thiết kế CSDL

Cơ sở dữ liệu SQL Server (tên cơ sở dữ liệu lấy từ biến `DB_NAME` trong `.env`). Cấu trúc bảng được tạo bằng 19 file Liquibase trong `src/main/resources/db/changelog/changes`. Khóa chính các bảng nghiệp vụ là `UUID`.

### 2.2.1. Danh sách bảng

| STT | Bảng                        | Mô tả                                  |
| --- | --------------------------- | -------------------------------------- |
| 1   | `users`                     | Tài khoản người dùng                   |
| 2   | `roles`                     | Vai trò                                |
| 3   | `permissions`               | Quyền                                  |
| 4   | `user_roles`                | Liên kết tài khoản – vai trò           |
| 5   | `role_permissions`          | Liên kết vai trò – quyền               |
| 6   | `refresh_tokens`            | Phiên đăng nhập (refresh token)        |
| 7   | `patients`                  | Hồ sơ người bệnh                       |
| 8   | `medical_services`          | Danh mục dịch vụ y tế                  |
| 9   | `locations`                 | Địa điểm khám                          |
| 10  | `location_shifts`           | Ca làm việc của địa điểm               |
| 11  | `contracts`                 | Hợp đồng khám                          |
| 12  | `contract_patients`         | Hồ sơ khám (người bệnh trong hợp đồng) |
| 13  | `contract_locations`        | Địa điểm khám được gán cho hợp đồng    |
| 14  | `exam_packages`             | Gói khám của hợp đồng                  |
| 15  | `package_services`          | Dịch vụ thuộc gói khám                 |
| 16  | `service_quotas`            | Định mức dịch vụ                       |
| 17  | `discount_policies`         | Chính sách giảm giá                    |
| 18  | `discount_policy_contracts` | Hợp đồng áp dụng chính sách giảm giá   |
| 19  | `discount_policy_tiers`     | Bậc chiết khấu                         |
| 20  | `receipts`                  | Phiếu thu                              |

📷 **[CHÈN ẢNH – Hình 2.3: Sơ đồ quan hệ thực thể (ERD)]** Chụp sơ đồ ERD từ công cụ quản trị CSDL (ví dụ DBeaver, Azure Data Studio hoặc SSMS: Database Diagrams) kết nối tới SQL Server `localhost:1433`, cơ sở dữ liệu `SaleDB` (hoặc tên bạn đặt trong `.env`).

### 2.2.2. Chi tiết các bảng

> **Quy ước:** PK = khóa chính; NN = bắt buộc (NOT NULL); UQ = duy nhất; FK = khóa ngoại có ràng buộc trong CSDL; "Tham chiếu" = cột liên kết tới bảng khác ở mức ứng dụng (JPA), chưa có ràng buộc khóa ngoại trong CSDL. Kiểu dữ liệu lấy từ các file Liquibase. Mọi bảng đều có khóa chính `id` kiểu `uniqueidentifier` (UUID).

**Nhóm cột dùng chung ở nhiều bảng:** `created_at`/`updated_at` là thời điểm tạo và cập nhật gần nhất; `created_by`/`updated_by` là người tạo và người cập nhật gần nhất; `is_deleted`, `deleted_at`, `deleted_by` dùng cho xóa mềm (đánh dấu đã xóa, thời điểm xóa, người xóa).

#### a) Bảng `users` – Tài khoản người dùng

| STT | Tên cột            | Kiểu dữ liệu     | Ràng buộc | Mô tả                                               |
| --- | ------------------ | ---------------- | --------- | --------------------------------------------------- |
| 1   | id                 | uniqueidentifier | PK        | Mã định danh tài khoản                              |
| 2   | name               | nvarchar(250)    | NN        | Họ và tên                                           |
| 3   | email              | varchar(100)     | NN, UQ    | Email, dùng làm tên đăng nhập                       |
| 4   | staff_code         | nvarchar(50)     |           | Mã nhân viên (gắn với tài khoản HIS)                |
| 5   | phone              | varchar(20)      |           | Số điện thoại                                       |
| 6   | active             | bit              | NN        | Tài khoản đang hoạt động (true) hay đã khóa (false) |
| 7   | password_hash      | varchar(255)     | NN        | Mật khẩu đã băm BCrypt                              |
| 8   | last_login_at      | datetime2        |           | Thời điểm đăng nhập thành công gần nhất             |
| 9   | locked_until       | datetime2        |           | Thời điểm hết khóa tạm do sai mật khẩu              |
| 10  | failed_login_count | int              |           | Số lần đăng nhập sai liên tiếp                      |
| 11  | created_at         | datetime2        | NN        | Thời điểm tạo                                       |
| 12  | updated_at         | datetime2        | NN        | Thời điểm cập nhật                                  |

#### b) Bảng `roles` – Vai trò

| STT | Tên cột     | Kiểu dữ liệu     | Ràng buộc | Mô tả                              |
| --- | ----------- | ---------------- | --------- | ---------------------------------- |
| 1   | id          | uniqueidentifier | PK        | Mã định danh vai trò               |
| 2   | code        | varchar(50)      | NN, UQ    | Mã vai trò (ví dụ `ADMIN`)         |
| 3   | name        | nvarchar(100)    | NN        | Tên vai trò                        |
| 4   | description | nvarchar(250)    |           | Mô tả phạm vi quyền hạn            |
| 5   | active      | bit              | NN        | Vai trò đang hoạt động hay đã khóa |
| 6   | created_at  | datetime2        | NN        | Thời điểm tạo                      |
| 7   | updated_at  | datetime2        | NN        | Thời điểm cập nhật                 |

#### c) Bảng `permissions` – Quyền

| STT | Tên cột     | Kiểu dữ liệu     | Ràng buộc | Mô tả                                            |
| --- | ----------- | ---------------- | --------- | ------------------------------------------------ |
| 1   | id          | uniqueidentifier | PK        | Mã định danh quyền                               |
| 2   | code        | varchar(200)     | UQ        | Mã quyền (ví dụ `USER_VIEW`)                     |
| 3   | name        | nvarchar(255)    |           | Tên quyền hiển thị                               |
| 4   | module      | varchar(50)      |           | Nhóm chức năng chứa quyền (USER, ROLE, SERVICE…) |
| 5   | description | nvarchar(255)    |           | Mô tả quyền                                      |
| 6   | created_at  | datetime2        | NN        | Thời điểm tạo                                    |
| 7   | updated_at  | datetime2        | NN        | Thời điểm cập nhật                               |

#### d) Bảng `user_roles` – Liên kết tài khoản – vai trò

| STT | Tên cột | Kiểu dữ liệu     | Ràng buộc      | Mô tả                      |
| --- | ------- | ---------------- | -------------- | -------------------------- |
| 1   | id      | uniqueidentifier | PK             | Mã định danh bản ghi       |
| 2   | user_id | uniqueidentifier | NN, FK → users | Tài khoản được gán vai trò |
| 3   | role_id | uniqueidentifier | NN, FK → roles | Vai trò được gán           |

Ràng buộc duy nhất theo cặp (`user_id`, `role_id`).

#### e) Bảng `role_permissions` – Liên kết vai trò – quyền

| STT | Tên cột       | Kiểu dữ liệu     | Ràng buộc            | Mô tả                      |
| --- | ------------- | ---------------- | -------------------- | -------------------------- |
| 1   | id            | uniqueidentifier | PK                   | Mã định danh bản ghi       |
| 2   | role_id       | uniqueidentifier | NN, FK → roles       | Vai trò                    |
| 3   | permission_id | uniqueidentifier | NN, FK → permissions | Quyền được cấp cho vai trò |

Ràng buộc duy nhất theo cặp (`role_id`, `permission_id`).

#### f) Bảng `refresh_tokens` – Phiên đăng nhập

| STT | Tên cột    | Kiểu dữ liệu     | Ràng buộc      | Mô tả                                                   |
| --- | ---------- | ---------------- | -------------- | ------------------------------------------------------- |
| 1   | id         | uniqueidentifier | PK             | Mã định danh phiên                                      |
| 2   | user_id    | uniqueidentifier | NN, FK → users | Tài khoản sở hữu phiên                                  |
| 3   | token_hash | varchar(255)     | NN, UQ         | Bản băm SHA-256 của refresh token (không lưu token gốc) |
| 4   | expires_at | datetime2        | NN             | Thời điểm hết hạn                                       |
| 5   | revoked_at | datetime2        |                | Thời điểm bị thu hồi (null = còn hiệu lực)              |
| 6   | ip_address | varchar(45)      |                | Địa chỉ IP                                              |
| 7   | user_agent | nvarchar(500)    |                | Thông tin trình duyệt                                   |
| 8   | created_at | datetime2        | NN             | Thời điểm tạo                                           |

Có chỉ mục `ix_refresh_tokens_user_id` theo `user_id`.

#### g) Bảng `patients` – Hồ sơ người bệnh

| STT | Tên cột      | Kiểu dữ liệu     | Ràng buộc     | Mô tả                               |
| --- | ------------ | ---------------- | ------------- | ----------------------------------- |
| 1   | id           | uniqueidentifier | PK            | Mã định danh                        |
| 2   | patient_code | varchar(50)      | NN, UQ        | Mã người bệnh (BN00001…)            |
| 3   | full_name    | nvarchar(255)    | NN            | Họ và tên                           |
| 4   | birth_year   | int              |               | Năm sinh                            |
| 5   | gender       | varchar(20)      | NN            | Giới tính: `NAM`, `NU`, `KHAC`      |
| 6   | phone        | varchar(20)      |               | Số điện thoại                       |
| 7   | address      | nvarchar(500)    |               | Địa chỉ thường trú                  |
| 8   | note         | nvarchar(1000)   |               | Ghi chú (bệnh nền, tiền sử dị ứng…) |
| 9   | is_deleted   | bit              | NN, DEFAULT 0 | Đã xóa mềm                          |
| 10  | deleted_at   | datetime2        |               | Thời điểm xóa                       |
| 11  | deleted_by   | nvarchar(100)    |               | Người xóa                           |
| 12  | created_by   | nvarchar(100)    |               | Người tạo                           |
| 13  | updated_by   | nvarchar(100)    |               | Người cập nhật                      |
| 14  | created_at   | datetime2        | NN            | Thời điểm tạo                       |
| 15  | updated_at   | datetime2        | NN            | Thời điểm cập nhật                  |

#### h) Bảng `medical_services` – Dịch vụ y tế

| STT | Tên cột      | Kiểu dữ liệu     | Ràng buộc     | Mô tả                                                                      |
| --- | ------------ | ---------------- | ------------- | -------------------------------------------------------------------------- |
| 1   | id           | uniqueidentifier | PK            | Mã định danh                                                               |
| 2   | code         | varchar(50)      | NN, UQ        | Mã dịch vụ (XN_001, CDHA_001…)                                             |
| 3   | name         | nvarchar(255)    | NN            | Tên dịch vụ                                                                |
| 4   | price        | decimal(18,2)    | NN, DEFAULT 0 | Đơn giá (VNĐ)                                                              |
| 5   | service_type | varchar(50)      | NN            | Loại: `XET_NGHIEM`, `CHAN_DOAN_HINH_ANH`, `KHAM_BENH`, `THU_THUAT`, `KHAC` |
| 6   | active       | bit              | NN, DEFAULT 1 | Đang áp dụng hay tạm dừng                                                  |
| 7   | description  | nvarchar(500)    |               | Mô tả chuyên môn                                                           |
| 8   | is_deleted   | bit              | NN, DEFAULT 0 | Đã xóa mềm                                                                 |
| 9   | deleted_at   | datetime2        |               | Thời điểm xóa                                                              |
| 10  | deleted_by   | nvarchar(100)    |               | Người xóa                                                                  |
| 11  | created_by   | nvarchar(100)    |               | Người tạo                                                                  |
| 12  | updated_by   | nvarchar(100)    |               | Người cập nhật                                                             |
| 13  | created_at   | datetime2        | NN            | Thời điểm tạo                                                              |
| 14  | updated_at   | datetime2        | NN            | Thời điểm cập nhật                                                         |

#### i) Bảng `locations` – Địa điểm khám

| STT | Tên cột    | Kiểu dữ liệu     | Ràng buộc     | Mô tả                       |
| --- | ---------- | ---------------- | ------------- | --------------------------- |
| 1   | id         | uniqueidentifier | PK            | Mã định danh                |
| 2   | code       | varchar(50)      | NN, UQ        | Mã địa điểm (tự sinh)       |
| 3   | name       | nvarchar(255)    | NN            | Tên địa điểm                |
| 4   | address    | nvarchar(500)    |               | Địa chỉ                     |
| 5   | active     | bit              | NN, DEFAULT 1 | Đang hoạt động hay tạm dừng |
| 6   | is_deleted | bit              | NN, DEFAULT 0 | Đã xóa mềm                  |
| 7   | deleted_at | datetime2        |               | Thời điểm xóa               |
| 8   | deleted_by | nvarchar(100)    |               | Người xóa                   |
| 9   | created_by | nvarchar(100)    |               | Người tạo                   |
| 10  | updated_by | nvarchar(100)    |               | Người cập nhật              |
| 11  | created_at | datetime2        | NN            | Thời điểm tạo               |
| 12  | updated_at | datetime2        | NN            | Thời điểm cập nhật          |

#### j) Bảng `location_shifts` – Ca làm việc của địa điểm

| STT | Tên cột      | Kiểu dữ liệu     | Ràng buộc          | Mô tả                             |
| --- | ------------ | ---------------- | ------------------ | --------------------------------- |
| 1   | id           | uniqueidentifier | PK                 | Mã định danh                      |
| 2   | location_id  | uniqueidentifier | NN, FK → locations | Địa điểm chứa ca                  |
| 3   | shift_number | int              | NN                 | Số thứ tự ca (1, 2…)              |
| 4   | shift_name   | nvarchar(50)     |                    | Tên ca                            |
| 5   | start_time   | varchar(10)      | NN                 | Giờ bắt đầu (dạng chuỗi giờ:phút) |
| 6   | end_time     | varchar(10)      | NN                 | Giờ kết thúc                      |
| 7   | created_at   | datetime2        | NN                 | Thời điểm tạo                     |

#### k) Bảng `contracts` – Hợp đồng khám

| STT | Tên cột       | Kiểu dữ liệu     | Ràng buộc              | Mô tả                                                             |
| --- | ------------- | ---------------- | ---------------------- | ----------------------------------------------------------------- |
| 1   | id            | uniqueidentifier | PK                     | Mã định danh                                                      |
| 2   | contract_code | varchar(50)      | NN, UQ                 | Mã hợp đồng (HD00001…)                                            |
| 3   | contract_name | nvarchar(255)    | NN                     | Tên hợp đồng                                                      |
| 4   | company_code  | varchar(50)      | NN                     | Mã công ty                                                        |
| 5   | company_name  | nvarchar(255)    | NN                     | Tên công ty/doanh nghiệp                                          |
| 6   | status        | varchar(30)      | NN, DEFAULT `HIEU_LUC` | Trạng thái: `HIEU_LUC`, `CHUA_HIEU_LUC`, `HET_HIEU_LUC`, `DA_HUY` |
| 7   | is_deleted    | bit              | NN, DEFAULT 0          | Đã xóa mềm                                                        |
| 8   | deleted_at    | datetime2        |                        | Thời điểm xóa                                                     |
| 9   | deleted_by    | nvarchar(100)    |                        | Người xóa                                                         |
| 10  | created_by    | nvarchar(100)    |                        | Người tạo                                                         |
| 11  | created_at    | datetime2        | NN                     | Thời điểm tạo                                                     |
| 12  | updated_by    | nvarchar(100)    |                        | Người cập nhật                                                    |
| 13  | updated_at    | datetime2        | NN                     | Thời điểm cập nhật                                                |

#### l) Bảng `contract_patients` – Hồ sơ khám (người bệnh trong hợp đồng)

| STT | Tên cột     | Kiểu dữ liệu     | Ràng buộc                  | Mô tả                                                          |
| --- | ----------- | ---------------- | -------------------------- | -------------------------------------------------------------- |
| 1   | id          | uniqueidentifier | PK                         | Mã định danh                                                   |
| 2   | contract_id | uniqueidentifier | NN, tham chiếu `contracts` | Hợp đồng                                                       |
| 3   | patient_id  | uniqueidentifier | NN, tham chiếu `patients`  | Người bệnh                                                     |
| 4   | record_code | varchar(50)      |                            | Mã hồ sơ khám                                                  |
| 5   | doctor_name | nvarchar(100)    |                            | Bác sĩ phụ trách                                               |
| 6   | exam_date   | date             |                            | Ngày khám                                                      |
| 7   | status      | varchar(30)      | NN, DEFAULT `MOI`          | Trạng thái hồ sơ: `MOI`, `CHO_DUYET`, `DANG_DUYET`, `DA_DUYET` |
| 8   | created_by  | nvarchar(100)    |                            | Người tạo                                                      |
| 9   | created_at  | datetime2        | NN                         | Thời điểm tạo                                                  |
| 10  | updated_at  | datetime2        | NN                         | Thời điểm cập nhật                                             |

#### m) Bảng `contract_locations` – Địa điểm khám gán cho hợp đồng

| STT | Tên cột      | Kiểu dữ liệu     | Ràng buộc                  | Mô tả                                              |
| --- | ------------ | ---------------- | -------------------------- | -------------------------------------------------- |
| 1   | id           | uniqueidentifier | PK                         | Mã định danh                                       |
| 2   | contract_id  | uniqueidentifier | NN, tham chiếu `contracts` | Hợp đồng                                           |
| 3   | location_id  | uniqueidentifier | NN, tham chiếu `locations` | Địa điểm khám                                      |
| 4   | active       | bit              | DEFAULT 1                  | Liên kết đang áp dụng hay tạm dừng                 |
| 5   | shift_config | nvarchar(255)    |                            | Cấu hình ca làm việc cho hợp đồng tại địa điểm này |
| 6   | created_by   | nvarchar(100)    |                            | Người gán                                          |
| 7   | created_at   | datetime2        | NN                         | Ngày gán                                           |
| 8   | updated_at   | datetime2        |                            | Thời điểm cập nhật                                 |

Ràng buộc duy nhất theo cặp (`contract_id`, `location_id`).

#### n) Bảng `exam_packages` – Gói khám của hợp đồng

| STT | Tên cột      | Kiểu dữ liệu     | Ràng buộc                  | Mô tả                                  |
| --- | ------------ | ---------------- | -------------------------- | -------------------------------------- |
| 1   | id           | uniqueidentifier | PK                         | Mã định danh                           |
| 2   | contract_id  | uniqueidentifier | NN, tham chiếu `contracts` | Hợp đồng sở hữu gói                    |
| 3   | package_code | varchar(50)      | NN                         | Mã gói (duy nhất trong cùng hợp đồng)  |
| 4   | package_name | nvarchar(255)    | NN                         | Tên gói khám                           |
| 5   | status       | varchar(30)      | NN, DEFAULT `HIEU_LUC`     | Trạng thái: `HIEU_LUC`, `HET_HIEU_LUC` |
| 6   | is_deleted   | bit              | NN, DEFAULT 0              | Đã xóa mềm                             |
| 7   | deleted_at   | datetime2        |                            | Thời điểm xóa                          |
| 8   | deleted_by   | nvarchar(100)    |                            | Người xóa                              |
| 9   | created_by   | nvarchar(100)    |                            | Người tạo                              |
| 10  | updated_by   | nvarchar(100)    |                            | Người cập nhật                         |
| 11  | created_at   | datetime2        | NN                         | Thời điểm tạo                          |
| 12  | updated_at   | datetime2        | NN                         | Thời điểm cập nhật                     |

Ràng buộc duy nhất theo cặp (`contract_id`, `package_code`).

#### o) Bảng `package_services` – Dịch vụ thuộc gói khám

| STT | Tên cột    | Kiểu dữ liệu     | Ràng buộc                         | Mô tả             |
| --- | ---------- | ---------------- | --------------------------------- | ----------------- |
| 1   | id         | uniqueidentifier | PK                                | Mã định danh      |
| 2   | package_id | uniqueidentifier | NN, tham chiếu `exam_packages`    | Gói khám          |
| 3   | service_id | uniqueidentifier | NN, tham chiếu `medical_services` | Dịch vụ trong gói |
| 4   | created_by | nvarchar(100)    |                                   | Người gán         |
| 5   | created_at | datetime2        | NN                                | Thời điểm gán     |

Ràng buộc duy nhất theo cặp (`package_id`, `service_id`).

#### p) Bảng `service_quotas` – Định mức dịch vụ

| STT | Tên cột         | Kiểu dữ liệu     | Ràng buộc                 | Mô tả                                   |
| --- | --------------- | ---------------- | ------------------------- | --------------------------------------- |
| 1   | id              | uniqueidentifier | PK                        | Mã định danh                            |
| 2   | service_id      | uniqueidentifier | NN, FK → medical_services | Dịch vụ áp dụng                         |
| 3   | location_id     | uniqueidentifier | NN, FK → locations        | Địa điểm khám                           |
| 4   | contract_id     | uniqueidentifier | FK → contracts            | Hợp đồng áp dụng (null = áp dụng chung) |
| 5   | apply_type      | varchar(50)      | NN, DEFAULT `THEO_CA`     | Cách áp dụng định mức                   |
| 6   | max_quantity    | int              | NN, DEFAULT 0             | Số lượng tối đa                         |
| 7   | shift1_quantity | int              | DEFAULT 0                 | Số lượng ở ca 1                         |
| 8   | shift2_quantity | int              | DEFAULT 0                 | Số lượng ở ca 2                         |
| 9   | shift_details   | nvarchar(max)    |                           | Chi tiết cấu hình theo ca               |
| 10  | valid_from      | date             |                           | Áp dụng từ ngày                         |
| 11  | valid_to        | date             |                           | Áp dụng đến ngày                        |
| 12  | active          | bit              | NN, DEFAULT 1             | Đang áp dụng hay tạm dừng               |
| 13  | created_by      | nvarchar(100)    |                           | Người tạo                               |
| 14  | updated_by      | nvarchar(100)    |                           | Người cập nhật                          |
| 15  | created_at      | datetime2        | NN                        | Thời điểm tạo                           |
| 16  | updated_at      | datetime2        |                           | Thời điểm cập nhật                      |

#### q) Bảng `discount_policies` – Chính sách giảm giá

| STT | Tên cột                  | Kiểu dữ liệu     | Ràng buộc                  | Mô tả                                                                                    |
| --- | ------------------------ | ---------------- | -------------------------- | ---------------------------------------------------------------------------------------- |
| 1   | id                       | uniqueidentifier | PK                         | Mã định danh                                                                             |
| 2   | code                     | varchar(50)      | NN, UQ                     | Mã chính sách (CK-001…)                                                                  |
| 3   | name                     | nvarchar(255)    | NN                         | Tên chính sách                                                                           |
| 4   | apply_type               | varchar(50)      | NN, DEFAULT `HOP_DONG`     | Loại áp dụng                                                                             |
| 5   | discount_type            | varchar(50)      | NN, DEFAULT `PERCENT`      | Hình thức: `PERCENT` (phần trăm), `AMOUNT` (số tiền)                                     |
| 6   | tier_scope               | varchar(50)      | NN, DEFAULT `MAX_TIER_ALL` | Phạm vi áp dụng bậc: `MAX_TIER_ALL` (bậc lớn nhất cho toàn bộ), `PROGRESSIVE` (lũy tiến) |
| 7   | max_discount_per_service | decimal(18,2)    |                            | Chiết khấu tối đa trên mỗi dịch vụ (null = không giới hạn)                               |
| 8   | start_date               | date             | NN                         | Áp dụng từ ngày                                                                          |
| 9   | end_date                 | date             |                            | Áp dụng đến ngày                                                                         |
| 10  | active                   | bit              | NN, DEFAULT 1              | Đang áp dụng hay ngừng                                                                   |
| 11  | created_by               | nvarchar(100)    |                            | Người tạo                                                                                |
| 12  | created_at               | datetime2        | NN                         | Thời điểm tạo                                                                            |
| 13  | updated_by               | nvarchar(100)    |                            | Người cập nhật                                                                           |
| 14  | updated_at               | datetime2        | NN                         | Thời điểm cập nhật                                                                       |
| 15  | is_deleted               | bit              | NN, DEFAULT 0              | Đã xóa mềm                                                                               |
| 16  | deleted_by               | nvarchar(100)    |                            | Người xóa                                                                                |
| 17  | deleted_at               | datetime2        |                            | Thời điểm xóa                                                                            |

#### r) Bảng `discount_policy_contracts` – Hợp đồng áp dụng chính sách

| STT | Tên cột     | Kiểu dữ liệu     | Ràng buộc                  | Mô tả               |
| --- | ----------- | ---------------- | -------------------------- | ------------------- |
| 1   | id          | uniqueidentifier | PK                         | Mã định danh        |
| 2   | policy_id   | uniqueidentifier | NN, FK → discount_policies | Chính sách giảm giá |
| 3   | contract_id | uniqueidentifier | NN, FK → contracts         | Hợp đồng được hưởng |
| 4   | created_by  | nvarchar(100)    |                            | Người gán           |
| 5   | created_at  | datetime2        | NN                         | Thời điểm gán       |

Ràng buộc duy nhất theo cặp (`policy_id`, `contract_id`).

#### s) Bảng `discount_policy_tiers` – Bậc chiết khấu

| STT | Tên cột        | Kiểu dữ liệu     | Ràng buộc                  | Mô tả                                                |
| --- | -------------- | ---------------- | -------------------------- | ---------------------------------------------------- |
| 1   | id             | uniqueidentifier | PK                         | Mã định danh                                         |
| 2   | policy_id      | uniqueidentifier | NN, FK → discount_policies | Chính sách chứa bậc                                  |
| 3   | tier_number    | int              | NN                         | Số thứ tự bậc                                        |
| 4   | from_quantity  | int              | NN                         | Từ số lượng dịch vụ                                  |
| 5   | to_quantity    | int              |                            | Đến số lượng dịch vụ (null = không giới hạn trên)    |
| 6   | discount_value | decimal(18,2)    | NN                         | Mức chiết khấu (% hoặc số tiền, tùy `discount_type`) |
| 7   | created_by     | nvarchar(100)    |                            | Người tạo                                            |
| 8   | created_at     | datetime2        | NN                         | Thời điểm tạo                                        |

#### t) Bảng `receipts` – Phiếu thu

| STT | Tên cột           | Kiểu dữ liệu     | Ràng buộc            | Mô tả                                                           |
| --- | ----------------- | ---------------- | -------------------- | --------------------------------------------------------------- |
| 1   | id                | uniqueidentifier | PK                   | Mã định danh                                                    |
| 2   | receipt_code      | varchar(50)      | NN, UQ               | Mã phiếu thu (`yyMMdd` + số thứ tự 5 chữ số)                    |
| 3   | patient_id        | uniqueidentifier |                      | Mã định danh người bệnh (bản sao, không có khóa ngoại)          |
| 4   | patient_code      | varchar(50)      |                      | Mã người bệnh                                                   |
| 5   | record_code       | varchar(50)      |                      | Mã hồ sơ khám                                                   |
| 6   | patient_name      | nvarchar(255)    | NN                   | Tên người bệnh                                                  |
| 7   | gender            | varchar(20)      |                      | Giới tính (`NAM`, `NU`…)                                        |
| 8   | birth_year        | varchar(20)      |                      | Năm sinh                                                        |
| 9   | contract_id       | uniqueidentifier |                      | Mã định danh hợp đồng                                           |
| 10  | company_code      | varchar(50)      |                      | Mã công ty                                                      |
| 11  | company_name      | nvarchar(255)    |                      | Tên công ty                                                     |
| 12  | contract_code     | varchar(50)      |                      | Mã hợp đồng                                                     |
| 13  | contract_name     | nvarchar(255)    |                      | Tên hợp đồng                                                    |
| 14  | package_name      | nvarchar(255)    |                      | Tên gói khám                                                    |
| 15  | lab_services      | nvarchar(max)    |                      | Danh sách dịch vụ xét nghiệm trong phiếu                        |
| 16  | imaging_services  | nvarchar(max)    |                      | Danh sách dịch vụ chẩn đoán hình ảnh trong phiếu                |
| 17  | all_services_json | nvarchar(max)    |                      | Toàn bộ dịch vụ đã chọn, dạng JSON                              |
| 18  | subtotal          | decimal(18,2)    |                      | Tạm tính (trước chiết khấu)                                     |
| 19  | discount_code     | varchar(50)      |                      | Mã chính sách giảm giá đã áp dụng                               |
| 20  | discount_percent  | decimal(5,2)     |                      | Phần trăm chiết khấu                                            |
| 21  | discount_amount   | decimal(18,2)    |                      | Số tiền chiết khấu                                              |
| 22  | final_amount      | decimal(18,2)    | NN                   | Thành tiền phải thu                                             |
| 23  | customer_paid     | decimal(18,2)    |                      | Khách thanh toán                                                |
| 24  | change_amount     | decimal(18,2)    |                      | Tiền thừa hoàn lại                                              |
| 25  | payment_method    | varchar(50)      |                      | Hình thức: `CHUYEN_KHOAN`, `TIEN_MAT`, `QUET_THE`               |
| 26  | cashier_name      | nvarchar(100)    |                      | Tên thu ngân                                                    |
| 27  | note              | nvarchar(500)    |                      | Ghi chú                                                         |
| 28  | print_status      | varchar(30)      | DEFAULT `DA_IN`      | Trạng thái in: `DA_IN`, `LUU_NHAP`, `LOI_IN`                    |
| 29  | sync_status       | varchar(30)      | DEFAULT `DA_DONG_BO` | Trạng thái đồng bộ: `DA_DONG_BO`, `CHUA_DONG_BO`, `LOI_DONG_BO` |
| 30  | created_by        | nvarchar(100)    |                      | Người tạo                                                       |
| 31  | created_at        | datetime2        |                      | Thời điểm lập phiếu                                             |
| 32  | updated_at        | datetime2        |                      | Thời điểm cập nhật                                              |
| 33  | is_deleted        | bit              | DEFAULT 0            | Đã xóa mềm                                                      |

Phiếu thu lưu bản sao thông tin người bệnh, công ty, hợp đồng và dịch vụ tại thời điểm lập, nên không dùng khóa ngoại tới các bảng đó.

---

## 2.3. Giao diện chính

Giao diện quản trị dùng chung một khung (`layout/admin-layout.html`) với thanh menu bên trái có 4 nhóm: _Quản trị hệ thống_, _Quản lý khám bệnh_, _Kinh doanh & Hợp đồng_, _Thiết lập chung_, và mục _Đăng xuất_ ở cuối.

### 2.3.1. Giao diện đăng nhập

Trang đăng nhập có tiêu đề "Chào mừng trở lại!", ô tên đăng nhập (nhập mã nhân viên hoặc email), ô mật khẩu có nút hiện/ẩn mật khẩu, ảnh nền bệnh viện.

📷 **[CHÈN ẢNH – Hình 2.4: Trang đăng nhập]** Chụp tại `http://localhost:8080/login`.

> **Mô tả nghiệp vụ:** Người dùng nhập tên đăng nhập (email) và mật khẩu. Hệ thống kiểm tra tài khoản có tồn tại, còn hoạt động, không đang bị khóa tạm và mật khẩu đúng. Đăng nhập đúng thì chuyển vào trang quản trị; sai thì hiện thông báo lỗi ngay trên form. Sai mật khẩu 5 lần sẽ bị khóa đăng nhập 15 phút.

### 2.3.2. Giao diện quản trị

📷 **[CHÈN ẢNH – Hình 2.5: Trang Danh mục dịch vụ (trang chủ quản trị)]** `http://localhost:8080/admin/services` (cũng là đích của `/`, `/admin`, `/admin/dashboard`).

> **Mô tả nghiệp vụ:** Trang đầu tiên sau khi đăng nhập, dùng để quản lý danh mục dịch vụ y tế (mã, tên, loại, đơn giá, mô tả, trạng thái). Người dùng tìm theo mã hoặc tên, lọc theo loại dịch vụ và trạng thái (Đang áp dụng/Tạm dừng), thêm, sửa, bật/tắt hoặc xóa dịch vụ. Danh mục này là nguồn để gán dịch vụ vào gói khám, định mức và phiếu thu.

📷 **[CHÈN ẢNH – Hình 2.6: Danh sách tài khoản]** `http://localhost:8080/admin/users`

> **Mô tả nghiệp vụ:** Quản trị viên quản lý tài khoản đăng nhập của nhân viên: tìm theo mã hoặc tên nhân viên, lọc theo vai trò và trạng thái. Mỗi tài khoản hiển thị mã nhân viên, tên, tên đăng nhập, vai trò và trạng thái; có thể sửa, khóa/mở khóa hoặc xóa.

📷 **[CHÈN ẢNH – Hình 2.7: Danh sách vai trò & phân quyền]** `http://localhost:8080/admin/roles`

> **Mô tả nghiệp vụ:** Hiển thị các vai trò trong hệ thống cùng mô tả, số tài khoản đang dùng, số quyền và trạng thái (Hoạt động/Đã khóa). Vai trò `ADMIN` không thể khóa hoặc xóa; vai trò đang có tài khoản sử dụng không thể xóa.

📷 **[CHÈN ẢNH – Hình 2.8: Màn hình chỉnh sửa vai trò & phân quyền]** `http://localhost:8080/admin/roles/create`

> **Mô tả nghiệp vụ:** Màn hình riêng để tạo hoặc sửa vai trò: nhập tên, mã và mô tả, sau đó tích chọn các quyền được cấp (có ô tìm theo tên hoặc mã quyền). Mã vai trò không được trùng.

📷 **[CHÈN ẢNH – Hình 2.9: Hồ sơ người bệnh]** `http://localhost:8080/admin/patients`

> **Mô tả nghiệp vụ:** Quản lý hồ sơ người bệnh: mã (tự sinh dạng BN00001), họ tên, năm sinh, giới tính, số điện thoại, địa chỉ, ghi chú bệnh nền. Tìm theo mã, tên, số điện thoại hoặc địa chỉ và lọc theo giới tính. Xóa hồ sơ là xóa mềm.

📷 **[CHÈN ẢNH – Hình 2.10: Quản lý hợp đồng]** `http://localhost:8080/admin/contracts`

> **Mô tả nghiệp vụ:** Quản lý hợp đồng khám với công ty: mã hợp đồng (tự sinh dạng HD00001), tên hợp đồng, mã và tên công ty, trạng thái (Hiệu lực, Chưa hiệu lực, Hết hiệu lực, Đã hủy). Từ danh sách, người dùng mở trang chi tiết của từng hợp đồng.

📷 **[CHÈN ẢNH – Hình 2.11: Chi tiết hợp đồng – tab Hồ sơ khám / Địa điểm khám / Gói khám]** Mở một hợp đồng trong danh sách, URL dạng `http://localhost:8080/admin/contracts/{id}`; chụp mỗi tab một ảnh.

> **Mô tả nghiệp vụ:** Trang chi tiết có 3 tab. _Hồ sơ khám_: các người bệnh thuộc hợp đồng, kèm mã hồ sơ, bác sĩ phụ trách, ngày khám và trạng thái hồ sơ. _Địa điểm khám_: các địa điểm được gán cho hợp đồng. _Gói khám_: các gói khám của hợp đồng và dịch vụ trong từng gói. Từ tab Hồ sơ khám có thể bán dịch vụ và lập phiếu thu cho từng người bệnh.

📷 **[CHÈN ẢNH – Hình 2.12: Danh sách phiếu thu]** `http://localhost:8080/admin/receipts`

> **Mô tả nghiệp vụ:** Tổng hợp các phiếu thu đã lập. Người dùng tìm theo mã NB, mã hồ sơ, tên NB; lọc theo công ty, người thu, hình thức thanh toán, khoảng thời gian và trạng thái đồng bộ; xem chi tiết, đổi trạng thái đồng bộ hoặc xuất danh sách ra file CSV.

📷 **[CHÈN ẢNH – Hình 2.13: Địa điểm khám]** `http://localhost:8080/admin/locations`

> **Mô tả nghiệp vụ:** Quản lý các địa điểm khám cùng giờ làm việc của Ca 1 và Ca 2. Từ mỗi địa điểm có thể gán các hợp đồng được khám tại đó. Địa điểm có thể bật/tắt hoặc xóa mềm.

📷 **[CHÈN ẢNH – Hình 2.14: Định mức dịch vụ]** `http://localhost:8080/admin/service-quotas`

> **Mô tả nghiệp vụ:** Hiển thị từng dịch vụ kèm số lượng áp dụng; mở rộng dòng để xem định mức theo địa điểm, hợp đồng và ca. Định mức giới hạn số lượng dịch vụ có thể thực hiện, có thể áp dụng chung hoặc cho một hợp đồng cụ thể và có thời gian hiệu lực.

📷 **[CHÈN ẢNH – Hình 2.15: Chính sách giảm giá]** `http://localhost:8080/admin/discounts`

> **Mô tả nghiệp vụ:** Danh sách chính sách chiết khấu gồm mã, tên, loại áp dụng, giá trị chiết khấu, thời gian áp dụng và trạng thái áp dụng. Có thể lọc theo hình thức (Phần trăm/Số tiền) và trạng thái, bật/tắt hoặc xóa chính sách.

---

## 2.4. Thiết kế xác thực và phân quyền

### 2.4.1. Kiến trúc tổng thể

- Mô hình: ứng dụng Spring MVC nhiều lớp theo từng module: `controller` → `service` → `repository` → `entity`, kèm `dto` cho dữ liệu vào/ra.
- Giao diện (Thymeleaf) chỉ trả về khung trang; dữ liệu được JavaScript gọi qua REST API (`fetch`).
- Spring Security chạy ở chế độ **không lưu session** (`STATELESS`), tắt CSRF; bộ lọc `JwtAuthenticationFilter` xác thực mỗi yêu cầu.
- Lỗi nghiệp vụ được xử lý tập trung ở `GlobalExceptionHandler` với `AuthException`, `BadRequestException`, `NotFoundException`.

### 2.4.2. Cơ chế access token và refresh token

- **Access token (JWT)** sống 15 phút, **refresh token** sống 7 ngày (`application.yaml`).
- Access token được lấy từ header `Authorization: Bearer ...` hoặc cookie `accessToken`. Refresh token lấy từ header `X-Refresh-Token` hoặc cookie `refreshToken`.
- Refresh token là chuỗi ngẫu nhiên; **cơ sở dữ liệu chỉ lưu bản băm SHA-256**.
- Khi làm mới token, token cũ bị thu hồi và cấp cặp token mới (xoay vòng).
- Đăng xuất thu hồi refresh token và xóa cookie.

### 2.4.3. Quy tắc bảo vệ tài khoản

- Mật khẩu băm bằng BCrypt.
- Sai mật khẩu **5 lần** thì khóa đăng nhập **15 phút**.
- Tài khoản bị khóa (`active = false`) không đăng nhập hoặc làm mới token được.

### 2.4.4. Phân quyền theo vai trò

- Các API `/api/users`, `/api/roles`, `/api/services`, `/api/locations`, `/api/patients`, `/api/contracts`, `/api/discounts`, `/api/service-quotas`, `/api/receipts` yêu cầu vai trò **`ADMIN`**; các API còn lại yêu cầu đã xác thực.
- Các đường dẫn `/`, `/login`, `/logout`, `/admin/**`, tài nguyên tĩnh (`/css`, `/js`, `/images`) và `/api/auth/*` được phép truy cập công khai (trang quản trị chỉ là khung giao diện, dữ liệu phải lấy qua API).
- Chưa có kiểm tra quyền chi tiết theo từng mã quyền (như `USER_VIEW`) trên API; các mã quyền được lưu và gán cho vai trò nhưng việc áp dụng hiện chỉ dừng ở kiểm tra vai trò `ADMIN`.

---

# III. TRIỂN KHAI

## 3.1. Cấu trúc project

```
PhongKham/
├── build.gradle, settings.gradle, gradlew
├── docker-compose.yml          # SQL Server 2022 + container tạo database
├── .env / .env.example         # biến môi trường
└── src/main/
    ├── java/mirai/sale/
    │   ├── SaleApplication.java
    │   ├── common/             # PageResponse, cấu hình QueryDSL, GlobalExceptionHandler + các exception
    │   ├── security/           # SecurityConfig, JwtService, JwtAuthenticationFilter
    │   ├── Controller/         # AdminViewController (các trang Thymeleaf), ProductController
    │   ├── seeder/             # tạo dữ liệu mẫu khi khởi động
    │   ├── TmpCheck/           # mã kiểm tra tạm
    │   └── modules/
    │       ├── auth/  user/  role/
    │       ├── patient/  service/  location/  quota/
    │       ├── contract/  discount/  receipt/
    │       └── medicalrecord/  medicalservice/   # mới có thư mục rỗng
    └── resources/
        ├── application.yaml
        ├── db/changelog/       # db.changelog-master.yml + changes/001…019
        ├── templates/          # layout/, admin/*.html, login.html
        └── static/             # css/, js/, images/
```

Mỗi module nghiệp vụ gồm `controller`, `service`, `repository`, `entity`, `dto`. Module `user` có thêm truy vấn QueryDSL tùy biến (`IUserQuery`).

### 3.1.1. Cấu hình và bảo mật

- Cấu hình ở `application.yaml`: kết nối SQL Server `localhost:1433` (người dùng `sa`), `ddl-auto: validate`, Liquibase đọc `db/changelog/db.changelog-master.yml`, Thymeleaf tắt cache, thời hạn token.
- **Không để thông tin bí mật trong mã nguồn**: mật khẩu SQL Server (`MSSQL_SA_PASSWORD` / `DB_PASSWORD`), `JWT_SECRET`, tài khoản admin mẫu (`ADMIN_NAME`, `ADMIN_EMAIL`, `ADMIN_PASSWORD`) lấy từ file `.env` (không đưa lên Git; có `.env.example` làm mẫu).
- Tài khoản admin được tạo sẵn khi khởi động nếu `ADMIN_SEED_ENABLED=true` (mặc định), email mặc định `admin@phongkham.local`.

## 3.2. Các chức năng đã làm

> Mỗi hình dưới đây gồm: vị trí chèn ảnh (URL/thao tác cần chụp) và phần mô tả nghiệp vụ của màn hình đó.

**3.2.1. Đăng nhập, đăng xuất**

📷 **[CHÈN ẢNH – Hình 3.1: Đăng nhập thành công]** Tại `http://localhost:8080/login`, nhập đúng tài khoản rồi chụp trang quản trị hiển thị sau khi chuyển trang.

> **Mô tả nghiệp vụ:** Đăng nhập đúng thì hệ thống cấp access token (15 phút) và refresh token (7 ngày), lưu vào cookie HttpOnly rồi chuyển sang `/admin/dashboard`. Bấm _Đăng xuất_ ở cuối menu thì refresh token bị thu hồi, cookie bị xóa và quay lại trang đăng nhập kèm thông báo đăng xuất thành công.

📷 **[CHÈN ẢNH – Hình 3.2: Thông báo lỗi khi sai mật khẩu]** Tại `http://localhost:8080/login`, nhập sai mật khẩu rồi chụp.

> **Mô tả nghiệp vụ:** Hệ thống báo lỗi (ví dụ "Sai mật khẩu", "Email không đúng", "Tài khoản của bạn đang bị khóa"). Sai 5 lần liên tiếp thì tài khoản bị khóa 15 phút và thông báo nêu thời điểm hết khóa.

**3.2.2. Quản lý tài khoản**

📷 **[CHÈN ẢNH – Hình 3.3: Danh sách tài khoản]** `http://localhost:8080/admin/users`

> **Mô tả nghiệp vụ:** Tìm theo mã hoặc tên nhân viên, lọc theo vai trò và trạng thái (Hoạt động/Đã khóa), phân trang 10/20/50/100. Tại mỗi dòng có thể sửa, khóa/mở khóa hoặc xóa tài khoản. Khi xóa, hệ thống xóa luôn liên kết vai trò và các phiên đăng nhập của tài khoản đó.

📷 **[CHÈN ẢNH – Hình 3.4: Cửa sổ Thêm tài khoản]** Tại `/admin/users`, bấm nút thêm tài khoản.

> **Mô tả nghiệp vụ:** Nhập họ tên, nhân sự (mã nhân viên, gắn với tài khoản HIS), tên đăng nhập (email), số điện thoại, mật khẩu và chọn vai trò hệ thống. Email phải chưa được sử dụng và vai trò chọn phải tồn tại; mật khẩu được băm BCrypt trước khi lưu.

**3.2.3. Quản lý vai trò & quyền**

📷 **[CHÈN ẢNH – Hình 3.5: Danh sách vai trò]** `http://localhost:8080/admin/roles`

> **Mô tả nghiệp vụ:** Hiển thị mã, tên, mô tả, số tài khoản đang dùng, quyền hạn và trạng thái của từng vai trò; lọc Hoạt động/Đã khóa. Không thể khóa hay xóa vai trò `ADMIN`; vai trò đang có tài khoản sử dụng không thể xóa (hệ thống báo số tài khoản đang dùng).

📷 **[CHÈN ẢNH – Hình 3.6: Chỉnh sửa vai trò & phân quyền]** `http://localhost:8080/admin/roles/create`

> **Mô tả nghiệp vụ:** Nhập tên, mã, mô tả vai trò và tích chọn các quyền thuộc 7 nhóm (Người dùng, Vai trò, Dịch vụ y tế, Địa điểm khám, Hồ sơ người bệnh, Hợp đồng, Chính sách giảm giá). Khi lưu, danh sách quyền của vai trò được đồng bộ theo lựa chọn. Mã vai trò không được trùng.

**3.2.4. Hồ sơ người bệnh**

📷 **[CHÈN ẢNH – Hình 3.7: Danh sách người bệnh]** `http://localhost:8080/admin/patients`

> **Mô tả nghiệp vụ:** Tìm theo mã BN, họ tên, số điện thoại hoặc địa chỉ; lọc theo giới tính Nam/Nữ/Khác. Mỗi dòng có thể sửa hoặc xóa. Xóa là xóa mềm (lưu người xóa và thời điểm xóa), không mất dữ liệu.

📷 **[CHÈN ẢNH – Hình 3.8: Cửa sổ Thêm mới người bệnh]** Tại `/admin/patients`, bấm nút thêm mới.

> **Mô tả nghiệp vụ:** Nhập mã người bệnh (hệ thống gợi ý mã kế tiếp dạng BN00001), họ tên, năm sinh, giới tính, số điện thoại, địa chỉ thường trú, ghi chú bệnh nền/tiền sử dị ứng. Mã người bệnh không được trùng.

**3.2.5. Quản lý hợp đồng**

📷 **[CHÈN ẢNH – Hình 3.9: Danh sách hợp đồng]** `http://localhost:8080/admin/contracts`

> **Mô tả nghiệp vụ:** Tìm theo mã hợp đồng, tên hợp đồng, mã hoặc tên công ty; lọc theo trạng thái. Cột "Người tạo" và "Người sửa gần nhất" cho biết ai đã thao tác. Chọn một hợp đồng để vào trang chi tiết; xóa hợp đồng là xóa mềm.

📷 **[CHÈN ẢNH – Hình 3.10: Cửa sổ Thêm mới hợp đồng]** Tại `/admin/contracts`, bấm nút thêm mới.

> **Mô tả nghiệp vụ:** Mã hợp đồng được tự sinh (HD00001, HD00002…) nhưng vẫn kiểm tra không trùng. Nhập tên hợp đồng, mã công ty, tên công ty và chọn trạng thái (mặc định là Hiệu lực).

**3.2.6. Chi tiết hợp đồng**

📷 **[CHÈN ẢNH – Hình 3.11: Tab Hồ sơ khám]** `http://localhost:8080/admin/contracts/{id}`

> **Mô tả nghiệp vụ:** Danh sách người bệnh đã được thêm vào hợp đồng, gồm mã NB, mã hồ sơ, bác sĩ, ngày khám, trạng thái hồ sơ (Mới, Chờ duyệt, Đang duyệt, Đã duyệt) và cột đồng bộ. Có tìm kiếm theo mã NB, mã hồ sơ, tên bệnh nhân, bác sĩ và lọc theo trạng thái hồ sơ. Đây là nơi bắt đầu thao tác bán dịch vụ cho từng người bệnh.

📷 **[CHÈN ẢNH – Hình 3.12: Cửa sổ Thêm bệnh nhân vào hợp đồng]** Tại tab Hồ sơ khám, bấm thêm bệnh nhân.

> **Mô tả nghiệp vụ:** Chọn người bệnh từ danh sách hồ sơ người bệnh, nhập mã hồ sơ khám, bác sĩ phụ trách, ngày khám và trạng thái hồ sơ. Một người bệnh không thể được thêm hai lần vào cùng một hợp đồng.

📷 **[CHÈN ẢNH – Hình 3.13: Tab Địa điểm khám và cửa sổ Gán địa điểm]** Tại tab Địa điểm khám, chụp danh sách và cửa sổ gán.

> **Mô tả nghiệp vụ:** Hiển thị các địa điểm đã gán cho hợp đồng (mã, tên, địa chỉ, trạng thái, ngày gán). Chọn thêm địa điểm khám từ danh sách để gán, hoặc gỡ địa điểm khỏi hợp đồng. Một địa điểm không thể gán hai lần vào cùng hợp đồng. Chỉ các địa điểm này mới xuất hiện khi bán dịch vụ cho hợp đồng.

📷 **[CHÈN ẢNH – Hình 3.14: Tab Gói khám và cửa sổ Thêm mới gói khám]** Tại tab Gói khám, chụp danh sách và cửa sổ thêm gói.

> **Mô tả nghiệp vụ:** Mỗi hợp đồng có các gói khám riêng với mã gói, tên gói, số dịch vụ, ngày tạo, người tạo. Có thể thêm, sửa, xóa gói và đặt trạng thái Hiệu lực/Hết hiệu lực. Mã gói không được trùng trong cùng hợp đồng.

📷 **[CHÈN ẢNH – Hình 3.15: Cửa sổ Gán dịch vụ cho gói khám]** Tại tab Gói khám, bấm gán dịch vụ cho một gói.

> **Mô tả nghiệp vụ:** Hiển thị danh mục dịch vụ y tế (mã, tên, phân loại, đơn giá), có tìm nhanh và lọc theo phân loại. Người dùng tích chọn các dịch vụ thuộc gói; khi lưu, hệ thống thêm dịch vụ mới được tích và gỡ dịch vụ bị bỏ tích.

**3.2.7. Bán dịch vụ và lập phiếu thu**

📷 **[CHÈN ẢNH – Hình 3.16: Cửa sổ bán dịch vụ]** Tại `/admin/contracts/{id}`, tab Hồ sơ khám, bấm thao tác bán dịch vụ trên một bệnh nhân.

> **Mô tả nghiệp vụ:** Chọn gói khám trong hợp đồng, điểm khám (chỉ gồm địa điểm đã gán cho hợp đồng) và ca khám theo cấu hình của địa điểm. Dịch vụ thuộc gói khám được tích sẵn; người dùng có thể tìm, chọn thêm hoặc bỏ dịch vụ. Bảng hiển thị dịch vụ, đơn giá và cột "SL còn lại" (hiện là số minh họa trên giao diện, chưa lấy từ định mức thật).

📷 **[CHÈN ẢNH – Hình 3.17: Phiếu thu có dòng chiết khấu]** Sau khi chọn dịch vụ ở cửa sổ bán dịch vụ, chụp cửa sổ phiếu thu.

> **Mô tả nghiệp vụ:** Phiếu thu hiển thị thông tin người bệnh, danh sách dịch vụ và tạm tính theo đơn giá gốc. Hệ thống tìm chính sách giảm giá đang áp dụng của hợp đồng, xác định bậc theo tổng số dịch vụ rồi tính chiết khấu (theo phần trăm hoặc theo số tiền, có áp dụng mức giảm tối đa trên mỗi dịch vụ nếu có). Thành tiền = tạm tính − chiết khấu. Người dùng nhập khách thanh toán, tiền thừa hoàn lại, thu ngân, ghi chú và hình thức thanh toán rồi lưu thành phiếu thu.

**3.2.8. Danh sách phiếu thu**

📷 **[CHÈN ẢNH – Hình 3.18: Danh sách phiếu thu và bộ lọc]** `http://localhost:8080/admin/receipts`

> **Mô tả nghiệp vụ:** Hiển thị mã phiếu thu, thông tin người bệnh, công ty, tổng số dịch vụ, tiền đã thu, người thu và thời gian. Lọc theo công ty, người thu, hình thức thanh toán (Chuyển khoản, Tiền mặt, Quẹt thẻ), khoảng thời gian và trạng thái đồng bộ. Có thể đổi qua lại giữa Đã đồng bộ và Chưa đồng bộ.

📷 **[CHÈN ẢNH – Hình 3.19: Chi tiết phiếu thu]** Tại `/admin/receipts`, bấm xem chi tiết một phiếu.

> **Mô tả nghiệp vụ:** Hiển thị lại đầy đủ nội dung phiếu đã lập: người bệnh, hợp đồng, danh sách dịch vụ, tạm tính, chiết khấu, thành tiền, khách thanh toán, tiền thừa hoàn lại, thu ngân và ghi chú. Dữ liệu là bản sao tại thời điểm lập phiếu nên không đổi khi danh mục dịch vụ hay hợp đồng thay đổi sau đó.

📷 **[CHÈN ẢNH – Hình 3.20: File CSV xuất ra mở trong Excel]** Tại `/admin/receipts`, bấm xuất Excel rồi mở file tải về.

> **Mô tả nghiệp vụ:** Xuất danh sách phiếu thu hiện có ra file `Danh_sach_phieu_thu_<ngày>.csv` (mã hóa UTF-8 có BOM để Excel hiển thị đúng tiếng Việt), gồm STT, mã phiếu thu, mã NB, mã hồ sơ, tên người bệnh, giới tính, năm sinh, công ty, tổng SL dịch vụ, tiền đã thu, người thu.

**3.2.9. Địa điểm khám**

📷 **[CHÈN ẢNH – Hình 3.21: Danh sách địa điểm khám]** `http://localhost:8080/admin/locations`

> **Mô tả nghiệp vụ:** Hiển thị mã, tên địa điểm và giờ làm việc của Ca 1, Ca 2. Tìm theo mã hoặc tên. Có thể sửa, bật/tắt hoạt động, xóa mềm hoặc xem và quản lý các hợp đồng đã gán cho địa điểm.

📷 **[CHÈN ẢNH – Hình 3.22: Thêm mới địa điểm kèm ca làm việc]** Tại `/admin/locations`, bấm nút thêm mới.

> **Mô tả nghiệp vụ:** Mã địa điểm được tự sinh; nhập tên và cấu hình ca làm việc (số ca, giờ bắt đầu, giờ kết thúc). Mặc định có 2 ca. Ca làm việc được dùng khi bán dịch vụ và khi thiết lập định mức theo ca.

📷 **[CHÈN ẢNH – Hình 3.23: Thêm hợp đồng vào địa điểm khám]** Tại `/admin/locations`, mở thao tác thêm hợp đồng cho một địa điểm.

> **Mô tả nghiệp vụ:** Chọn hợp đồng áp dụng (có ô lọc theo mã hoặc tên) và cấu hình ca làm việc cho hợp đồng đó tại địa điểm. Một hợp đồng không thể gán hai lần vào cùng địa điểm; liên kết có thể bật/tắt hoặc gỡ bỏ.

**3.2.10. Danh mục dịch vụ**

📷 **[CHÈN ẢNH – Hình 3.24: Danh mục dịch vụ y tế]** `http://localhost:8080/admin/services`

> **Mô tả nghiệp vụ:** Danh sách dịch vụ gồm mã, tên, loại (Xét nghiệm, Chẩn đoán hình ảnh, Khám chuyên khoa, Thủ thuật, Khác), đơn giá, mô tả, người cập nhật và trạng thái. Lọc theo loại và trạng thái (Đang áp dụng/Tạm dừng); bật/tắt dịch vụ không làm mất dữ liệu.

📷 **[CHÈN ẢNH – Hình 3.25: Thêm dịch vụ mới]** Tại `/admin/services`, bấm nút thêm dịch vụ.

> **Mô tả nghiệp vụ:** Nhập mã dịch vụ (ví dụ XN_001, CDHA_001), tên, loại dịch vụ, đơn giá (VNĐ) và mô tả chuyên môn. Mã dịch vụ không được trùng.

**3.2.11. Định mức dịch vụ**

📷 **[CHÈN ẢNH – Hình 3.26: Danh sách định mức (mở rộng dòng)]** `http://localhost:8080/admin/service-quotas`, bấm mở rộng một dòng dịch vụ.

> **Mô tả nghiệp vụ:** Bảng cha liệt kê dịch vụ kèm số lượng áp dụng; bảng con (khi mở rộng) liệt kê các định mức của dịch vụ đó theo địa điểm, hợp đồng và ca. Có thể bật/tắt, sửa, xóa từng định mức hoặc đồng bộ từ HIS (giả lập).

📷 **[CHÈN ẢNH – Hình 3.27: Thiết lập định mức dịch vụ]** Tại `/admin/service-quotas`, bấm thiết lập định mức.

> **Mô tả nghiệp vụ:** Chọn dịch vụ, địa điểm khám, hợp đồng áp dụng (để trống thì áp dụng chung) và thời gian áp dụng; nhập số lượng tối đa và số lượng cho từng ca. Địa điểm phải đang hoạt động và đã có cấu hình ca; không cho tạo trùng định mức của cùng dịch vụ, địa điểm và hợp đồng.

**3.2.12. Chính sách giảm giá**

📷 **[CHÈN ẢNH – Hình 3.28: Danh sách chính sách giảm giá]** `http://localhost:8080/admin/discounts`

> **Mô tả nghiệp vụ:** Hiển thị mã, tên, loại áp dụng, giá trị chiết khấu, thời gian áp dụng và trạng thái. Tìm theo mã/tên chính sách hoặc tên công ty/hợp đồng; lọc theo hình thức giảm giá và trạng thái. Có thể bật/tắt hoặc xóa chính sách; chỉ chính sách đang áp dụng được dùng khi lập phiếu thu.

📷 **[CHÈN ẢNH – Hình 3.29: Form thêm chính sách với bảng bậc chiết khấu]** `http://localhost:8080/admin/discounts/create`

> **Mô tả nghiệp vụ:** Nhập tên chính sách (mã tự sinh), chọn hình thức chiết khấu (Phần trăm hoặc Số tiền) và lập bảng bậc: bậc, từ số lượng, đến số lượng, mức chiết khấu. Chọn phạm vi áp dụng bậc (bậc lớn nhất cho toàn bộ dịch vụ hoặc lũy tiến theo từng bậc), mức chiết khấu tối đa trên mỗi dịch vụ (có thể để trống = không giới hạn) và khoảng ngày áp dụng.

📷 **[CHÈN ẢNH – Hình 3.30: Chọn hợp đồng áp dụng]** Tại form chính sách, bấm chọn hợp đồng áp dụng.

> **Mô tả nghiệp vụ:** Tìm theo mã hợp đồng, tên công ty hoặc tên hợp đồng và tích chọn một hoặc nhiều hợp đồng sẽ hưởng chính sách; hệ thống hiển thị số hợp đồng đã chọn.

## 3.3. Code tiêu biểu

Chọn đoạn mã ngắn (10–25 dòng) trong các file sau để dán vào báo cáo, kèm lời giải thích:

| Mục                                    | File gợi ý                                                                                                                                                                                                   |
| -------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| 3.3.1. Controller / API                | `modules/contract/controller/ContractDetailController.java`, `modules/receipt/controller/ReceiptController.java`                                                                                             |
| 3.3.2. Xử lý cơ sở dữ liệu / nghiệp vụ | `modules/receipt/service/ReceiptService.java` (sinh mã phiếu, phân loại dịch vụ, lưu JSON); `modules/quota/service/ServiceQuotaService.java`; `modules/user/repository/query/IUserQueryImpl.java` (QueryDSL) |
| 3.3.3. Xác thực & bảo mật              | `modules/auth/service/AuthService.java` (khóa tài khoản, xoay vòng refresh token); `security/SecurityConfig.java`; `security/JwtAuthenticationFilter.java`                                                   |
| 3.3.4. Giao diện frontend              | `static/js/contract-detail.js` (tính chiết khấu theo bậc, đoạn xử lý `matchedTier`); `static/js/receipts.js` (`exportExcel`)                                                                                 |
| 3.3.5. Cấu trúc CSDL                   | `db/changelog/changes/019-create-receipts.yaml`                                                                                                                                                              |

## 3.4. Kiểm thử

Mã nguồn hiện chỉ có một lớp kiểm thử mặc định `src/test/java/mirai/sale/SaleApplicationTests.java`. Phần này nên trình bày kết quả **kiểm thử thủ công** do bạn tự thực hiện theo mẫu sau (điền kết quả thực tế):

| STT | Chức năng                          | Các bước                             | Kết quả mong đợi                                | Kết quả thực tế |
| --- | ---------------------------------- | ------------------------------------ | ----------------------------------------------- | --------------- |
| 1   | Đăng nhập                          | Nhập đúng email/mật khẩu             | Vào `/admin/dashboard`                          | `[điền]`        |
| 2   | Đăng nhập sai                      | Nhập sai mật khẩu 5 lần              | Tài khoản bị khóa 15 phút                       | `[điền]`        |
| 3   | Thêm hợp đồng trùng mã             | Nhập mã đã tồn tại                   | Báo "Mã hợp đồng '…' đã tồn tại"                | `[điền]`        |
| 4   | Thêm người bệnh trùng vào hợp đồng | Thêm lại cùng người bệnh             | Báo "Người bệnh này đã được thêm vào hợp đồng!" | `[điền]`        |
| 5   | Xóa vai trò ADMIN                  | Bấm xóa                              | Báo không thể xóa vai trò Quản trị viên tối cao | `[điền]`        |
| 6   | Xóa vai trò đang có tài khoản      | Bấm xóa                              | Báo đang có N tài khoản sử dụng                 | `[điền]`        |
| 7   | Lập phiếu thu có giảm giá          | Chọn dịch vụ đủ số lượng của một bậc | Hiển thị dòng chiết khấu và thành tiền đúng     | `[điền]`        |
| 8   | Xuất CSV phiếu thu                 | Bấm xuất                             | Tải file `Danh_sach_phieu_thu_<ngày>.csv`       | `[điền]`        |

## 3.5. Hướng dẫn cài đặt và chạy

**Yêu cầu:** JDK 17, Docker (để chạy SQL Server).

1. Sao chép `.env.example` thành `.env` và điền: `MSSQL_SA_PASSWORD`, `DB_NAME`, `JWT_SECRET` (chuỗi Base64 của khóa ngẫu nhiên), và nếu muốn đổi tài khoản admin mẫu thì `ADMIN_EMAIL`, `ADMIN_PASSWORD`.
2. Khởi động cơ sở dữ liệu: `docker compose up -d` (tạo SQL Server 2022 cổng 1433 và tự tạo database `DB_NAME`).
3. Chạy ứng dụng: `./gradlew bootRun`. Khi khởi động, Liquibase tạo/cập nhật bảng và các seeder tạo dữ liệu mẫu.
4. Mở trình duyệt: `http://localhost:8080/login` và đăng nhập bằng tài khoản admin mẫu.

Dữ liệu mẫu do seeder tạo: vai trò (`ADMIN`, `BAC_SI_KET_LUAN`, `NV_HO_SO`, `KY_THUAT_VIEN`, `BAC_SI`), quyền, tài khoản admin, danh mục dịch vụ (ví dụ `XN_001`, `CDHA_001`…), người bệnh, hợp đồng, chi tiết hợp đồng, địa điểm khám, định mức dịch vụ, chính sách giảm giá.

---

# IV. KẾT QUẢ & ĐÁNH GIÁ

## 4.1. Kết quả đạt được

- Hoàn thành giao diện quản trị với **11 nhóm chức năng** và REST API tương ứng.
- Quản lý được vòng nghiệp vụ: danh mục (dịch vụ, địa điểm, người bệnh) → hợp đồng (hồ sơ khám, địa điểm, gói khám) → định mức và giảm giá → bán dịch vụ và lập phiếu thu.
- Xác thực JWT với refresh token xoay vòng, khóa tài khoản khi đăng nhập sai, mật khẩu băm BCrypt.
- Cấu trúc CSDL được quản lý bằng Liquibase (19 changelog), có dữ liệu mẫu tự động.
- Xóa mềm cho các danh mục chính, có lưu vết người tạo/sửa/xóa.

## 4.2. Hạn chế

- Các API nghiệp vụ chỉ cho phép vai trò `ADMIN`; các mã quyền chi tiết chưa được kiểm tra trên từng API.
- Cột **"SL còn lại"** trong cửa sổ bán dịch vụ hiện là **số giả lập phía giao diện**, chưa lấy từ định mức dịch vụ trong CSDL.
- Chức năng **đồng bộ định mức từ HIS** là giả lập (mock); trạng thái đồng bộ của phiếu thu chỉ được đổi bằng thao tác thủ công, chưa kết nối hệ thống HIS thật.
- Mã phiếu thu được sinh từ tổng số phiếu hiện có chứ không đếm theo ngày, và phương thức sinh mã chỉ an toàn khi chạy một phiên bản ứng dụng.
- Module hồ sơ bệnh án (`medicalrecord`) và `medicalservice` chưa được cài đặt.
- Chỉ có bài kiểm thử mặc định, chưa có kiểm thử tự động cho nghiệp vụ.
- Chưa có trang tổng quan (dashboard) thống kê; `/admin/dashboard` hiện hiển thị trang Danh mục dịch vụ.

---

# V. KẾT LUẬN

Hệ thống PhongKham (Mirai Healthcare Admin) đáp ứng được yêu cầu quản trị cơ bản cho phòng khám khám sức khỏe theo hợp đồng: quản lý danh mục, hợp đồng, định mức, chính sách giảm giá và phiếu thu, với đăng nhập bảo mật bằng JWT. Qua đề tài, sinh viên áp dụng được Spring Boot, JPA/QueryDSL, Liquibase, Spring Security và xây dựng giao diện quản trị bằng Thymeleaf kết hợp JavaScript. Hướng phát triển gồm: kiểm tra quyền chi tiết theo mã quyền, lấy số lượng còn lại từ định mức thật, kết nối HIS thật, hoàn thiện module hồ sơ bệnh án và bổ sung kiểm thử tự động.

---

# VI. TÀI LIỆU THAM KHẢO

1. Spring Boot Reference Documentation – https://docs.spring.io/spring-boot/
2. Spring Security Reference – https://docs.spring.io/spring-security/reference/
3. Spring Data JPA Reference – https://docs.spring.io/spring-data/jpa/reference/
4. Liquibase Documentation – https://docs.liquibase.com/
5. Thymeleaf Documentation – https://www.thymeleaf.org/documentation.html
6. JJWT (Java JWT) – https://github.com/jwtk/jjwt
7. Microsoft SQL Server on Docker – https://learn.microsoft.com/sql/linux/sql-server-linux-docker-container-deployment
8. Phosphor Icons – https://phosphoricons.com/
9. _(Thêm giáo trình/slide môn Lập trình Web của giảng viên nếu có.)_
