# Hướng Dẫn Tự Động Hóa & Triển Khai Code - Nguyên (feat/nguyen)
> **Dành cho**: Thành viên trong nhóm hoặc **AI Coding Agent / Harness (Claude Code, Cursor, Copilot, Antigravity, Aider...)** thực hiện tự động từ A-Z.
> **Mục tiêu tối thượng**: Đảm bảo code được đồng bộ từ nhánh chính (`develop`), sao chép đúng các file được phân công, rebase/merge sạch sẽ để **TẠO PULL REQUEST (PR) KHÔNG BAO GIỜ XẢY RA CONFLICT (Merge Conflicts: 0%)**.

---

> [!WARNING]
> **QUY TẮC BẢO VỆ FILE `README.md` (CHỐNG CONFLICT 100%)**:
> - File `README.md` chính của repository chỉ do **anh Tài (Nhóm trưởng / PM Lead)** quản trị và push lên.
> - **TUYỆT ĐỐI KHÔNG** tạo, sửa, copy đè hay commit file `README.md` trong nhánh của bạn.
> - Nếu vô tình sửa nhầm, hãy khôi phục lại ngay trước khi commit:
>   ```bash
>   git checkout develop -- README.md
>   ```

## 📌 THÔNG TIN PHÂN CÔNG
- **Thành viên phụ trách**: Nguyên
- **Vai trò**: Backend Infra Lead
- **Branch Prefix**: `feat/nguyen`
- **Tên nhánh đề xuất**: `feat/nguyen-base-source`
- **Số lượng file đảm nhận**: 253 file(s)

---

## 🤖 PROMPT DÀNH CHO AI AGENT / HARNESS (COPY & DÁN VÀO AI ĐỂ CHẠY TỰ ĐỘNG)

Nếu bạn sử dụng AI Agent (như Cursor Composer, Claude Code, GitHub Copilot Chat, Antigravity, Aider), hãy dán toàn bộ đoạn prompt sau vào cửa sổ chat của Agent:

```markdown
Bạn là AI Coding Assistant hỗ trợ thành viên Nguyên. Nhiệm vụ của bạn là lấy mã nguồn được phân công cho Nguyên từ thư mục chứa code và đẩy lên repository Git theo đúng chuẩn Git Flow, đảm bảo 0% conflict khi tạo Pull Request vào nhánh 'develop'.

Hãy thực hiện tuần tự các bước sau trong terminal của repository 'soulflow-api':

1. Cập nhật nhánh chính 'develop':
   git checkout develop
   git pull origin develop

2. Tạo hoặc reset nhánh tính năng mới từ 'develop':
   git checkout -B feat/nguyen-base-source develop

3. Sao chép (Overwrite) các file mã nguồn từ thư mục phân chia (LƯU Ý: TUYỆT ĐỐI KHÔNG copy hoặc đè lên file README.md của repo, chỉ anh Tài mới push README.md):
   [ĐƯỜNG_DẪN_TỚI_THƯ_MỤC_HIỆN_TẠI_CỦA_BAN]/
   sang thư mục gốc của repository 'soulflow-api/'. Giữ nguyên cấu trúc thư mục con (src/main/..., etc.).

4. Kiểm tra trạng thái và đảm bảo code biên dịch thành công:
   git checkout develop -- README.md 2>/dev/null || true # Đảm bảo không dính file README.md
   git status
   ./mvnw clean compile -DskipTests

5. Đồng bộ lại một lần nữa với develop để triệt tiêu mọi nguy cơ conflict:
   git pull --rebase origin develop

6. Commit toàn bộ thay đổi theo chuẩn Conventional Commits:
   git add .
   git commit -m "feat(core): setup infra, docker, redis and base config"

7. Push nhánh tính năng lên remote repository:
   git push origin feat/nguyen-base-source --force

8. Báo cáo lại link hoặc hướng dẫn tạo Pull Request từ 'feat/nguyen-base-source' trỏ vào 'develop'.
```

---

## 🛠️ QUY TRÌNH THỦ CÔNG (NẾU TỰ CHẠY LỆNH BẰNG TAY)

Mở Terminal tại thư mục repository **`soulflow-api`** và chạy tuần tự các lệnh sau:

### Bước 1: Đồng bộ nhánh chính `develop` mới nhất
```bash
# Chuyển về develop và pull code mới nhất
git checkout develop
git pull origin develop
```

### Bước 2: Tạo nhánh chức năng xuất phát từ `develop`
```bash
git checkout -B feat/nguyen-base-source develop
```

### Bước 3: Copy các file được phân chia vào dự án
- Copy toàn bộ nội dung trong thư mục này (ngoại trừ file `HUONG_DAN_NHIEM_VU.md` và file `README.md`) dán đè vào thư mục dự án `soulflow-api`.
- *Lưu ý*: Chỉ sao chép đúng các file thuộc phạm vi chức năng của bạn như danh sách bên dưới, không can thiệp vào file của thành viên khác.

### Bước 4: Kiểm tra và Compile thử
```bash
# Trên Windows PowerShell:
.\mvnw.cmd clean compile -DskipTests

# Trên Linux/MacOS:
./mvnw clean compile -DskipTests
```

### Bước 5: Rebase với nhánh `develop` trước khi push (Tránh Conflict)
```bash
git pull --rebase origin develop
```

### Bước 6: Commit code
```bash
git add .
git commit -m "feat(core): setup infra, docker, redis and base config"
```

### Bước 7: Push lên remote repository
```bash
git push -u origin feat/nguyen-base-source
```

### Bước 8: Tạo Pull Request (PR) trên GitHub
- Truy cập vào GitHub repository của dự án.
- Nhấn **New Pull Request**.
- **Base (Đích)**: chọn `develop` *(TUYỆT ĐỐI KHÔNG CHỌN `main`)*.
- **Compare (Nguồn)**: chọn `feat/nguyen-base-source`.
- Đặt tiêu đề PR: `feat(core): setup infra, docker, redis and base config`.
- Bấm **Create Pull Request**.

---

## Danh Sách File Đảm Nhận (253 files)
- `.env.example`
- `.gitattributes`
- `.gitignore`
- `.mvn/wrapper/maven-wrapper.properties`
- `data.sql`
- `Dockerfile`
- `init/data.sql`
- `init/data_utf8.sql`
- `init/flower_shop.sql`
- `init/init_all.sql`
- `init/readme.txt`
- `minio-backup/.minio.sys/buckets/.bloomcycle.bin/xl.meta`
- `minio-backup/.minio.sys/buckets/.usage-cache.bin/xl.meta`
- `minio-backup/.minio.sys/buckets/.usage-cache.bin.bkp/xl.meta`
- `minio-backup/.minio.sys/buckets/.usage.json/xl.meta`
- `minio-backup/.minio.sys/buckets/flower-shop/.metadata.bin/xl.meta`
- `minio-backup/.minio.sys/buckets/flower-shop/.usage-cache.bin/xl.meta`
- `minio-backup/.minio.sys/buckets/flower-shop/.usage-cache.bin.bkp/xl.meta`
- `minio-backup/.minio.sys/config/config.json/xl.meta`
- `minio-backup/.minio.sys/config/iam/format.json/xl.meta`
- `minio-backup/.minio.sys/config/iam/sts/SRRG23SZ7S1EJAKFV3K3/identity.json/xl.meta`
- `minio-backup/.minio.sys/format.json`
- `minio-backup/.minio.sys/pool.bin/xl.meta`
- `minio-backup/.minio.sys/tmp/.trash/0292fc10-6491-48fe-a2a9-a4df36dc8964/xl.meta.bkp`
- `minio-backup/.minio.sys/tmp/.trash/05b066f7-b863-421e-9226-19efde6dec0c/xl.meta.bkp`
- `minio-backup/.minio.sys/tmp/.trash/0c308f45-82c3-4f02-a521-9e00c596597b/xl.meta.bkp`
- `minio-backup/.minio.sys/tmp/.trash/15dc9a23-f27f-4889-8bcc-8327a8d7e83f/xl.meta.bkp`
- `minio-backup/.minio.sys/tmp/.trash/1dae6ac0-e154-400c-8b69-c3cd8e37f831/xl.meta.bkp`
- `minio-backup/.minio.sys/tmp/.trash/228c1532-eb3a-4da2-8102-9903f44e5d0c/xl.meta.bkp`
- `minio-backup/.minio.sys/tmp/.trash/38026c71-cd65-4728-96f3-ff4093e3f290/xl.meta.bkp`
- `minio-backup/.minio.sys/tmp/.trash/4d62b616-11df-4a41-aab8-71489d0182d6/xl.meta.bkp`
- `minio-backup/.minio.sys/tmp/.trash/5a2dc9d6-8f0d-4c60-acb0-497f70aff588/xl.meta.bkp`
- `minio-backup/.minio.sys/tmp/.trash/74df5b5a-2709-4298-bbab-8fcad7366a0d/xl.meta.bkp`
- `minio-backup/.minio.sys/tmp/.trash/8c64c004-af4b-4fe4-929a-a89ff28b9878/xl.meta.bkp`
- `minio-backup/.minio.sys/tmp/.trash/984a085e-b1f5-435b-9cf6-8bb9416ea82e/xl.meta.bkp`
- `minio-backup/.minio.sys/tmp/.trash/9a6055e4-eea0-44cc-9318-9612103bdc89/xl.meta.bkp`
- `minio-backup/.minio.sys/tmp/.trash/9cf03db8-41d5-4d3a-9092-5a5e38fcb6e2/xl.meta.bkp`
- `minio-backup/.minio.sys/tmp/.trash/a57c71d6-c82b-461a-ab94-07a02b053f80/xl.meta.bkp`
- `minio-backup/.minio.sys/tmp/.trash/d0dbe783-9915-43ee-9a92-fee9a78d1aa6/xl.meta.bkp`
- `minio-backup/.minio.sys/tmp/.trash/f71108c3-ef60-4542-a23b-ad50d739a614/xl.meta.bkp`
- `minio-backup/.minio.sys/tmp/.trash/f9839e4c-a32e-4f59-8e17-f1b081cad583/xl.meta.bkp`
- `minio-backup/.minio.sys/tmp/a267b340-a85d-4548-82b9-a3cce37115ae`
- `minio-backup/flower-shop/00a77498-b008-4836-b0d2-d331a9550af1_BO-003_03.jpg/511aa309-63da-4120-8735-5b49d6140994/part.1`
- `minio-backup/flower-shop/00a77498-b008-4836-b0d2-d331a9550af1_BO-003_03.jpg/xl.meta`
- `minio-backup/flower-shop/028957fc-55e9-4e6b-8082-bdbe0bcfd2a2_4669_khoanh-khac-dang-nho.jpg/7936b889-3d25-4fef-9830-10ec211995b8/part.1`
- `minio-backup/flower-shop/028957fc-55e9-4e6b-8082-bdbe0bcfd2a2_4669_khoanh-khac-dang-nho.jpg/xl.meta`
- `minio-backup/flower-shop/03fd1e6f-69e2-4e06-851b-2102b207b5a6_4678_ngoi-nha-hanh-phuc.jpg/aa6ef3fe-3d0a-48b8-bf45-1b693e1d1ade/part.1`
- `minio-backup/flower-shop/03fd1e6f-69e2-4e06-851b-2102b207b5a6_4678_ngoi-nha-hanh-phuc.jpg/xl.meta`
- `minio-backup/flower-shop/04b7fb3c-f716-4912-90c2-091de236b385_BO-001_03.jpg/89881a56-17ad-496c-b3c7-381ddd69c06a/part.1`
- `minio-backup/flower-shop/04b7fb3c-f716-4912-90c2-091de236b385_BO-001_03.jpg/xl.meta`
- `minio-backup/flower-shop/0940db56-bfb8-44c9-a604-689fa24132f6_BO-002_03.jpg/8bf19788-8f85-4774-a53d-5a1a7d165f9b/part.1`
- `minio-backup/flower-shop/0940db56-bfb8-44c9-a604-689fa24132f6_BO-002_03.jpg/xl.meta`
- `minio-backup/flower-shop/0cf62c11-ff5c-4ad5-8daf-d59e22714c40_Ke-002_02.jpg/xl.meta`
- `minio-backup/flower-shop/0d75e6f0-8094-4c92-aebb-ff9b3f767f58_14903_khoanh-khac-dang-nho.jpg/bec0f40e-d331-41df-903f-f8562ca14e1f/part.1`
- `minio-backup/flower-shop/0d75e6f0-8094-4c92-aebb-ff9b3f767f58_14903_khoanh-khac-dang-nho.jpg/xl.meta`
- `minio-backup/flower-shop/0dafec71-4b4b-4309-9974-1d233ea0c5bd_BO-005_01.jpg/9eca1fcf-ad27-4dc0-a736-a1378203f6c2/part.1`
- `minio-backup/flower-shop/0dafec71-4b4b-4309-9974-1d233ea0c5bd_BO-005_01.jpg/xl.meta`
- `minio-backup/flower-shop/0db428c8-4ae9-4891-9135-a575c146717e_BO-001_04.jpg/15ef6c47-d129-4d35-a26d-82b0ab9887ef/part.1`
- `minio-backup/flower-shop/0db428c8-4ae9-4891-9135-a575c146717e_BO-001_04.jpg/xl.meta`
- `minio-backup/flower-shop/11c52abb-1150-4cf0-b546-e448e9160ef2_5603_chan-thanh.jpg/68a41349-165b-48d1-9d0f-400802b3874c/part.1`
- `minio-backup/flower-shop/11c52abb-1150-4cf0-b546-e448e9160ef2_5603_chan-thanh.jpg/xl.meta`
- `minio-backup/flower-shop/12e20ebc-2919-467a-b5a7-685908c06bed_4779_i-do.jpg/553ff9ac-7c48-4a16-adf4-081d23146fee/part.1`
- `minio-backup/flower-shop/12e20ebc-2919-467a-b5a7-685908c06bed_4779_i-do.jpg/xl.meta`
- `minio-backup/flower-shop/13f923ae-f296-4cb8-b0d3-38627aa47a69_GIO-006_03.jpg/xl.meta`
- `minio-backup/flower-shop/18b5a7f8-4985-4543-a271-8d489363744f_BO-004_02.jpg/befa5378-9339-437c-9d12-1e7692abc5f2/part.1`
- `minio-backup/flower-shop/18b5a7f8-4985-4543-a271-8d489363744f_BO-004_02.jpg/xl.meta`
- `minio-backup/flower-shop/197112d3-790f-49fb-9eb1-d737b4b4a1bc_/xl.meta`
- `minio-backup/flower-shop/1a3cee5e-cb40-4044-ba4c-966d727c566b_/xl.meta`
- `minio-backup/flower-shop/1bb786e9-2e1f-44e1-bd8c-6cadb26b8801_BO-002_04.jpg/e8ebbebe-8eb9-4a5a-9470-d24ab8628eff/part.1`
- `minio-backup/flower-shop/1bb786e9-2e1f-44e1-bd8c-6cadb26b8801_BO-002_04.jpg/xl.meta`
- `minio-backup/flower-shop/1ff969a3-5f91-48e7-971b-089bf490631b_avatar.webp/xl.meta`
- `minio-backup/flower-shop/202ce6f7-6e79-4301-a4d2-0ed21c4ec30a_BO-005_03.jpg/eb8ac640-3875-40e5-b0be-917e591c009b/part.1`
- `minio-backup/flower-shop/202ce6f7-6e79-4301-a4d2-0ed21c4ec30a_BO-005_03.jpg/xl.meta`
- `minio-backup/flower-shop/234e7a43-8a07-4f4d-9ed7-6d74307a0fdd_GIO-005_03.webp/bd266bcf-6a3c-4a43-9d60-17dc5af93ec2/part.1`
- `minio-backup/flower-shop/234e7a43-8a07-4f4d-9ed7-6d74307a0fdd_GIO-005_03.webp/xl.meta`
- `minio-backup/flower-shop/2402f464-e693-4bda-a7b6-b165fa55633d_3241_chan-thanh.jpg/f78bd7b3-6c61-4f92-9410-3d339aec03bc/part.1`
- `minio-backup/flower-shop/2402f464-e693-4bda-a7b6-b165fa55633d_3241_chan-thanh.jpg/xl.meta`
- `minio-backup/flower-shop/24e5f36f-0b77-477d-ab3b-7159051cc12c_GIO-003_03.webp/xl.meta`
- `minio-backup/flower-shop/254474a5-b7c1-43a6-86d7-5e8bbd704673_BO-003_02.jpg/6c3baf76-f9ed-423e-b140-e25bdb587b72/part.1`
- `minio-backup/flower-shop/254474a5-b7c1-43a6-86d7-5e8bbd704673_BO-003_02.jpg/xl.meta`
- `minio-backup/flower-shop/25aa9dca-18ff-44f9-bbf3-52b4a8b1ad43_HOP-005_01.jpg/64186897-d0e0-493e-b071-aee88e9d65dc/part.1`
- `minio-backup/flower-shop/25aa9dca-18ff-44f9-bbf3-52b4a8b1ad43_HOP-005_01.jpg/xl.meta`
- `minio-backup/flower-shop/2686834f-5a5a-4e33-a01f-24d7513fbb48_3222_love-me-tender.jpg/834638e9-cd08-439c-a740-2af842744ccb/part.1`
- `minio-backup/flower-shop/2686834f-5a5a-4e33-a01f-24d7513fbb48_3222_love-me-tender.jpg/xl.meta`
- `minio-backup/flower-shop/286857a4-2231-447f-bced-5a51c486614a_BO-001_05.jpg/95e7fed1-8515-42ba-a60c-732d1f5ca7c1/part.1`
- `minio-backup/flower-shop/286857a4-2231-447f-bced-5a51c486614a_BO-001_05.jpg/xl.meta`
- `minio-backup/flower-shop/294171ea-32a3-47f0-a17a-48572ee09784_5609_love-me-tender.jpg/fa699b64-403a-4daf-bb07-b068d2e30fb9/part.1`
- `minio-backup/flower-shop/294171ea-32a3-47f0-a17a-48572ee09784_5609_love-me-tender.jpg/xl.meta`
- `minio-backup/flower-shop/2c46a6bf-d0c8-4649-a9bc-ec5fcbb506f1_3925_tulip-love.jpg/d2145ad1-d571-4bb4-a592-4836532bf7a5/part.1`
- `minio-backup/flower-shop/2c46a6bf-d0c8-4649-a9bc-ec5fcbb506f1_3925_tulip-love.jpg/xl.meta`
- `minio-backup/flower-shop/2ce0f9ec-572b-42b6-a416-e387c92c8d81_HOP-003_02.jpg/1837d9cd-602e-4bbc-b6f1-c8d8cc09cd29/part.1`
- `minio-backup/flower-shop/2ce0f9ec-572b-42b6-a416-e387c92c8d81_HOP-003_02.jpg/xl.meta`
- `minio-backup/flower-shop/3206de3c-b174-43fc-81ee-148fe96803c6_HOP-006_03.jpg/f5a89693-2c2c-44e1-b203-ecd3011701d8/part.1`
- `minio-backup/flower-shop/3206de3c-b174-43fc-81ee-148fe96803c6_HOP-006_03.jpg/xl.meta`
- `minio-backup/flower-shop/371ddbdc-f733-43e0-b003-bc05573805ec_HOP-005_03.jpg/dc6f1c58-298a-432e-ad64-74aef05484fc/part.1`
- `minio-backup/flower-shop/371ddbdc-f733-43e0-b003-bc05573805ec_HOP-005_03.jpg/xl.meta`
- `minio-backup/flower-shop/37814d26-58fe-4641-afc7-042e7259a4cd_GIO-003_02.webp/xl.meta`
- `minio-backup/flower-shop/3883607f-08b6-4725-8121-30ae8755524a_peter.webp/xl.meta`
- `minio-backup/flower-shop/3c4d716e-f3a1-4b9d-a959-86ded71c9f8f_GIO-004_02.jpg/xl.meta`
- `minio-backup/flower-shop/3ca3fdaa-fef0-40f2-b58d-d8d192bb2eab_BO-004_01.jpg/2f2c70f2-5cdd-48d4-b244-bd4c1eeb738e/part.1`
- `minio-backup/flower-shop/3ca3fdaa-fef0-40f2-b58d-d8d192bb2eab_BO-004_01.jpg/xl.meta`
- `minio-backup/flower-shop/3ea46e58-31b9-47b7-926a-fb9fbc488d15_HOP-001_01.webp/835b41c9-0b94-4d60-8d33-cad0daaf62e4/part.1`
- `minio-backup/flower-shop/3ea46e58-31b9-47b7-926a-fb9fbc488d15_HOP-001_01.webp/xl.meta`
- `minio-backup/flower-shop/3f4d9153-b539-4c13-88ce-eb1da23a53f4_GIO-002_01.webp/20bf85a9-daaf-45f7-9a0b-7d666a5b3b53/part.1`
- `minio-backup/flower-shop/3f4d9153-b539-4c13-88ce-eb1da23a53f4_GIO-002_01.webp/xl.meta`
- `minio-backup/flower-shop/4132a515-a1d0-4ad6-b780-2afdc2d9237d_HOP-003_01.jpg/833dd504-7c27-4b35-ba4c-0811ad01bb25/part.1`
- `minio-backup/flower-shop/4132a515-a1d0-4ad6-b780-2afdc2d9237d_HOP-003_01.jpg/xl.meta`
- `minio-backup/flower-shop/41982052-3b20-4d7b-859c-43d3a7a7ad58_avatar.webp/xl.meta`
- `minio-backup/flower-shop/46338ccd-5d71-4a56-891a-b9edb64bf59c_HOP-002_04.jpg/d43598b3-77a7-411d-9656-2f2a9c1bd7cd/part.1`
- `minio-backup/flower-shop/46338ccd-5d71-4a56-891a-b9edb64bf59c_HOP-002_04.jpg/xl.meta`
- `minio-backup/flower-shop/4a24f3ed-1df6-459a-86ca-b8127e10072e_GIO-006_02.jpg/xl.meta`
- `minio-backup/flower-shop/4b124b8f-bc0c-4686-8cc3-ef33aa7fd492_Ke-003_02.jpg/xl.meta`
- `minio-backup/flower-shop/50ad3fb2-82d8-48a3-8cad-26557797c3f3_BO-003_04.jpg/d5435150-5201-44ac-87cb-e258a4210162/part.1`
- `minio-backup/flower-shop/50ad3fb2-82d8-48a3-8cad-26557797c3f3_BO-003_04.jpg/xl.meta`
- `minio-backup/flower-shop/5665ec7d-bb08-4517-a2b4-d31592f4469e_GIO-003_01.jpg/xl.meta`
- `minio-backup/flower-shop/5685934c-035f-4844-8837-49f718715c96_HOP-002_03.jpg/973f621c-bd33-4611-96ab-9fac336c53cd/part.1`
- `minio-backup/flower-shop/5685934c-035f-4844-8837-49f718715c96_HOP-002_03.jpg/xl.meta`
- `minio-backup/flower-shop/588df847-ec05-43c9-a7e7-4b8b2be32ccb_BO-003_01.jpg/28a0de09-a7b4-4a23-912e-091cc2ddc0e3/part.1`
- `minio-backup/flower-shop/588df847-ec05-43c9-a7e7-4b8b2be32ccb_BO-003_01.jpg/xl.meta`
- `minio-backup/flower-shop/5ae251ff-de41-42bb-b9b1-0aaedea9663f_Ke-003_01.jpg/xl.meta`
- `minio-backup/flower-shop/5c3e6718-2d6e-4e67-8919-972c4e056a12_GIO-001_03.jpg/xl.meta`
- `minio-backup/flower-shop/5cf888cc-52cd-42ee-9123-030bde67df2d_14901_ngoi-nha-hanh-phuc.jpg/5e62661e-2f3d-4459-822c-e00f08d95835/part.1`
- `minio-backup/flower-shop/5cf888cc-52cd-42ee-9123-030bde67df2d_14901_ngoi-nha-hanh-phuc.jpg/xl.meta`
- `minio-backup/flower-shop/5dec5468-3e45-4fbe-8a20-25c83a60d295_BO-001_01.jpg/2a6023a6-de15-44e0-90a3-36f7fd8bd53b/part.1`
- `minio-backup/flower-shop/5dec5468-3e45-4fbe-8a20-25c83a60d295_BO-001_01.jpg/xl.meta`
- `minio-backup/flower-shop/60866cb9-ee83-472b-be81-be34a2882df2_BO-006_03.jpg/7fb7740c-9f3c-4359-b5b7-7fcef000a383/part.1`
- `minio-backup/flower-shop/60866cb9-ee83-472b-be81-be34a2882df2_BO-006_03.jpg/xl.meta`
- `minio-backup/flower-shop/6d966582-8055-4697-b904-d1094aade446_avatar.webp/xl.meta`
- `minio-backup/flower-shop/6e496a60-56f4-4c60-a652-ba97a022a44a_4733_tulip-love.jpg/9d928199-db0a-4b74-8d00-aa3051d6e291/part.1`
- `minio-backup/flower-shop/6e496a60-56f4-4c60-a652-ba97a022a44a_4733_tulip-love.jpg/xl.meta`
- `minio-backup/flower-shop/70cc8520-bbce-4fec-bd30-923485282afa_/xl.meta`
- `minio-backup/flower-shop/734a9a6c-e77d-4ae0-8da0-4b761b97518d_HOP-006_01.jpg/9f659b47-6f9f-4a7c-82c0-4c12507486dd/part.1`
- `minio-backup/flower-shop/734a9a6c-e77d-4ae0-8da0-4b761b97518d_HOP-006_01.jpg/xl.meta`
- `minio-backup/flower-shop/75818181-d3df-4dee-9604-4f477e8f6015_avatar.webp/xl.meta`
- `minio-backup/flower-shop/785809bf-d8b6-4512-b7ef-150b7bff8949_avatar.webp/xl.meta`
- `minio-backup/flower-shop/801a7bca-380d-419d-82ca-da3ec3d0ccf6_GIO-005_02.jpg/d4940abd-c4b6-4b36-b38d-2f326092ad25/part.1`
- `minio-backup/flower-shop/801a7bca-380d-419d-82ca-da3ec3d0ccf6_GIO-005_02.jpg/xl.meta`
- `minio-backup/flower-shop/8622e390-0c17-4c3c-8659-023c20656047_HOP-001_02.webp/cf718743-47f1-40bd-ac15-25082c02af76/part.1`
- `minio-backup/flower-shop/8622e390-0c17-4c3c-8659-023c20656047_HOP-001_02.webp/xl.meta`
- `minio-backup/flower-shop/86e4ef7d-9954-443d-bd86-4ceb0f9747ba_BO-005_04.jpg/03ff0523-a02b-40e9-8932-e4c3826ce0d3/part.1`
- `minio-backup/flower-shop/86e4ef7d-9954-443d-bd86-4ceb0f9747ba_BO-005_04.jpg/xl.meta`
- `minio-backup/flower-shop/8798b026-d40d-4530-aa7a-8d1dc4a71f88_/xl.meta`
- `minio-backup/flower-shop/87d7437d-3074-4966-a590-71f851033aa2_BO-002_02.jpg/ef4eb2ab-bd45-42c4-b613-e98c791c8ddf/part.1`
- `minio-backup/flower-shop/87d7437d-3074-4966-a590-71f851033aa2_BO-002_02.jpg/xl.meta`
- `minio-backup/flower-shop/895d9b9f-7172-4963-b519-be6576f70cd8_GIO-002_03.jpg/72b59beb-ee9a-45fe-9387-52f2afc56857/part.1`
- `minio-backup/flower-shop/895d9b9f-7172-4963-b519-be6576f70cd8_GIO-002_03.jpg/xl.meta`
- `minio-backup/flower-shop/8d4f69f7-d380-47ba-8e69-2a75195a3b20_BO-005_02.jpg/9b4506cb-fde5-45a3-895a-e34a2051335a/part.1`
- `minio-backup/flower-shop/8d4f69f7-d380-47ba-8e69-2a75195a3b20_BO-005_02.jpg/xl.meta`
- `minio-backup/flower-shop/90d8959e-6376-4eca-a656-8c7ec91dcbcb_HOP-005_04.jpg/7c710819-6a00-45a4-b79f-ab5f295650c5/part.1`
- `minio-backup/flower-shop/90d8959e-6376-4eca-a656-8c7ec91dcbcb_HOP-005_04.jpg/xl.meta`
- `minio-backup/flower-shop/9155fbf9-7783-49e1-bcfb-1ac5821ef29a_/xl.meta`
- `minio-backup/flower-shop/9545379c-309b-48fa-832b-88fb65c2ce55_HOP-001_03.webp/b84d3392-f164-430b-96f2-e76dac490ab8/part.1`
- `minio-backup/flower-shop/9545379c-309b-48fa-832b-88fb65c2ce55_HOP-001_03.webp/xl.meta`
- `minio-backup/flower-shop/95ca6fdf-8d70-404c-901b-bbd4bec01132_/xl.meta`
- `minio-backup/flower-shop/962a2242-6433-4587-ac21-e10ccb1b6579_HOP-006_02.jpg/62b0d335-12d3-4bf3-9450-eb9a37fd31fe/part.1`
- `minio-backup/flower-shop/962a2242-6433-4587-ac21-e10ccb1b6579_HOP-006_02.jpg/xl.meta`
- `minio-backup/flower-shop/965459e4-70b1-4adf-809b-be29132143f6_BO-006_02.jpg/f606cd03-7ec7-45ad-90f3-4180edb8323f/part.1`
- `minio-backup/flower-shop/965459e4-70b1-4adf-809b-be29132143f6_BO-006_02.jpg/xl.meta`
- `minio-backup/flower-shop/9921a222-d122-4606-b7e6-cea7f6939f36_Ke-001_02.jpg/xl.meta`
- `minio-backup/flower-shop/9e6ebc13-37e0-4589-a8d3-6420925422d5_/xl.meta`
- `minio-backup/flower-shop/a1167f37-e39a-40d4-b46e-97da10e7cc22_peter.webp/xl.meta`
- `minio-backup/flower-shop/a53ce53d-53e1-4f4d-9507-a4a6b4c1b154_/xl.meta`
- `minio-backup/flower-shop/a567c453-4b76-4d1d-aa16-9f29b6a5d170_GIO-001_02.jpg/xl.meta`
- `minio-backup/flower-shop/a6c1612d-2de7-4f05-a358-933b6077d5e8_BO-001_02.jpg/7d68994e-e994-450c-bdd3-16b6712c9074/part.1`
- `minio-backup/flower-shop/a6c1612d-2de7-4f05-a358-933b6077d5e8_BO-001_02.jpg/xl.meta`
- `minio-backup/flower-shop/ab40c035-a9b5-482b-a3b9-a2d5e4891067_BO-002_02.jpg/28fbc9c6-0b65-425d-a906-7b00ff78301e/part.1`
- `minio-backup/flower-shop/ab40c035-a9b5-482b-a3b9-a2d5e4891067_BO-002_02.jpg/xl.meta`
- `minio-backup/flower-shop/ad95568d-48be-4e0d-beea-2812e9dfbbb2_GIO-006_01.jpg/xl.meta`
- `minio-backup/flower-shop/aecdc205-2406-4b60-a595-c703d3b44d04_/xl.meta`
- `minio-backup/flower-shop/aed3e8f8-5030-4c9b-a813-c4223dc0a9bc_Ke-001_01.jpg/xl.meta`
- `minio-backup/flower-shop/b154ee09-5556-48d5-94f9-0d1e86e0abfb_BO-001_01.jpg/756daa7b-c650-42e4-9561-868338fe3a9f/part.1`
- `minio-backup/flower-shop/b154ee09-5556-48d5-94f9-0d1e86e0abfb_BO-001_01.jpg/xl.meta`
- `minio-backup/flower-shop/b1c14ab9-dec8-4f4e-84a4-623631b9e698_HOP-004_03.jpg/f964a16b-f8bf-462a-87e7-a5de77d11f3d/part.1`
- `minio-backup/flower-shop/b1c14ab9-dec8-4f4e-84a4-623631b9e698_HOP-004_03.jpg/xl.meta`
- `minio-backup/flower-shop/b546d604-68d7-4841-8271-b083c45c59be_HOP-003_04.jpg/d9868067-a037-4bc7-9695-548863f1afc2/part.1`
- `minio-backup/flower-shop/b546d604-68d7-4841-8271-b083c45c59be_HOP-003_04.jpg/xl.meta`
- `minio-backup/flower-shop/b5973bfb-7126-4617-9399-8f9fb4bd8a86_HOP-003_05.jpg/800a5341-d749-4037-8d7b-124a2a1f883c/part.1`
- `minio-backup/flower-shop/b5973bfb-7126-4617-9399-8f9fb4bd8a86_HOP-003_05.jpg/xl.meta`
- `minio-backup/flower-shop/b5d7bdbc-89da-4614-9a62-c63790150be3_HOP-004_04.jpg/11c4391e-881e-4741-88ce-6f8cecb5bf06/part.1`
- `minio-backup/flower-shop/b5d7bdbc-89da-4614-9a62-c63790150be3_HOP-004_04.jpg/xl.meta`
- `minio-backup/flower-shop/b9a0bcdc-6e42-4f77-8e4f-d9f404c15175_GIO-004_01.webp/215411a9-666f-404f-9509-8b9a9e92409e/part.1`
- `minio-backup/flower-shop/b9a0bcdc-6e42-4f77-8e4f-d9f404c15175_GIO-004_01.webp/xl.meta`
- `minio-backup/flower-shop/be64f327-2813-4519-af0d-db24cf7cd47b_4701_ngoi-nha-hanh-phuc.jpg/fc192913-1357-47d6-af1e-21fd6513ba10/part.1`
- `minio-backup/flower-shop/be64f327-2813-4519-af0d-db24cf7cd47b_4701_ngoi-nha-hanh-phuc.jpg/xl.meta`
- `minio-backup/flower-shop/c0f05556-6678-43c8-b809-54f8430f34da_BO-004_03.jpg/56aa1dc2-ee6d-4edb-9a07-03a88d776d4d/part.1`
- `minio-backup/flower-shop/c0f05556-6678-43c8-b809-54f8430f34da_BO-004_03.jpg/xl.meta`
- `minio-backup/flower-shop/c4c29abc-9c45-471e-9152-7cda4358b151_BO-006_04.jpg/d4dddb81-34ca-4c94-afc9-8e98dd8c1958/part.1`
- `minio-backup/flower-shop/c4c29abc-9c45-471e-9152-7cda4358b151_BO-006_04.jpg/xl.meta`
- `minio-backup/flower-shop/c574756b-a82d-4674-8998-aa9654bce955_GIO-002_02.jpg/a1095744-cfe6-461f-982c-ddc9ef74abfe/part.1`
- `minio-backup/flower-shop/c574756b-a82d-4674-8998-aa9654bce955_GIO-002_02.jpg/xl.meta`
- `minio-backup/flower-shop/c657e743-c1a2-4162-b20c-fe230693fae6_4686_i-do.jpg/c27fb90d-8bf0-429f-98e9-6eafb215265f/part.1`
- `minio-backup/flower-shop/c657e743-c1a2-4162-b20c-fe230693fae6_4686_i-do.jpg/xl.meta`
- `minio-backup/flower-shop/c6912d90-47e2-4fe5-b31d-be8399753375_HOP-005_02.jpg/e71a37dc-dcd3-44f8-903f-0085ad0d29f7/part.1`
- `minio-backup/flower-shop/c6912d90-47e2-4fe5-b31d-be8399753375_HOP-005_02.jpg/xl.meta`
- `minio-backup/flower-shop/c7e38028-8850-432f-b901-0f360e88a7c4_Ke-002_01.jpg/xl.meta`
- `minio-backup/flower-shop/cb074ce6-f41f-4cef-8937-825962ce1092_HOP-004_02.jpg/2add4ad8-25c2-4dd6-9ade-88e24ca45516/part.1`
- `minio-backup/flower-shop/cb074ce6-f41f-4cef-8937-825962ce1092_HOP-004_02.jpg/xl.meta`
- `minio-backup/flower-shop/cb305802-8305-4958-b99e-a3b7166237fe_HOP-003_03.jpg/5f9b8519-afea-4a18-9816-2ff973660fcf/part.1`
- `minio-backup/flower-shop/cb305802-8305-4958-b99e-a3b7166237fe_HOP-003_03.jpg/xl.meta`
- `minio-backup/flower-shop/d5c5e503-9fa8-4493-8df8-f0e9b20883ec_/xl.meta`
- `minio-backup/flower-shop/d753483d-b6ef-4275-bac3-68e449dd98df_BO-001_02.jpg/88851796-8d3d-4b70-849d-f5c832f50cff/part.1`
- `minio-backup/flower-shop/d753483d-b6ef-4275-bac3-68e449dd98df_BO-001_02.jpg/xl.meta`
- `minio-backup/flower-shop/dccef534-2cf9-442a-b68e-0f67652508d6_BO-006_01.jpg/7f96c986-bbb2-4453-824d-61eb66c22c5a/part.1`
- `minio-backup/flower-shop/dccef534-2cf9-442a-b68e-0f67652508d6_BO-006_01.jpg/xl.meta`
- `minio-backup/flower-shop/dcf8b458-407d-4ba8-899c-19d9fbcd6a1f_HOP-002_01.jpg/51d4dd65-543e-468f-9f64-a5f369646c38/part.1`
- `minio-backup/flower-shop/dcf8b458-407d-4ba8-899c-19d9fbcd6a1f_HOP-002_01.jpg/xl.meta`
- `minio-backup/flower-shop/de64c3b0-0725-4f8e-acec-36e970c2153b_BO-001_03.jpg/d7fb2c2a-86a0-4914-b372-6dbc891967c2/part.1`
- `minio-backup/flower-shop/de64c3b0-0725-4f8e-acec-36e970c2153b_BO-001_03.jpg/xl.meta`
- `minio-backup/flower-shop/e0121b05-a792-4920-a192-310ed593ffc8_GIO-004_03.jpg/9001d084-ef13-425a-a23e-ce77958ffe44/part.1`
- `minio-backup/flower-shop/e0121b05-a792-4920-a192-310ed593ffc8_GIO-004_03.jpg/xl.meta`
- `minio-backup/flower-shop/e3154573-41dd-4671-b29c-b2b75168bbaf_GIO-005_01.jpg/9b4b12b6-535d-4463-86d8-b04f7c343bbb/part.1`
- `minio-backup/flower-shop/e3154573-41dd-4671-b29c-b2b75168bbaf_GIO-005_01.jpg/xl.meta`
- `minio-backup/flower-shop/e510c384-d650-4e73-897e-a6e9a60ff64b_/xl.meta`
- `minio-backup/flower-shop/e54d9065-645d-42a1-bb26-f627f4f56700_HOP-004_01.jpg/b7a5201e-83fe-4629-a3ed-7e2eb2a1c576/part.1`
- `minio-backup/flower-shop/e54d9065-645d-42a1-bb26-f627f4f56700_HOP-004_01.jpg/xl.meta`
- `minio-backup/flower-shop/ea76d83e-3212-45ff-a3a6-e5a460406153_5608_love-me-tender.jpg/352113e1-ffa9-411f-aa3a-679c1d299051/part.1`
- `minio-backup/flower-shop/ea76d83e-3212-45ff-a3a6-e5a460406153_5608_love-me-tender.jpg/xl.meta`
- `minio-backup/flower-shop/eab6bcb8-b215-47c4-8c26-7dc2f562a800_HOP-002_02.jpg/0ff3687f-43e3-4ab5-8785-56b995ad7f1e/part.1`
- `minio-backup/flower-shop/eab6bcb8-b215-47c4-8c26-7dc2f562a800_HOP-002_02.jpg/xl.meta`
- `minio-backup/flower-shop/ef000046-8152-4d33-b34c-820c52f6a9bf_/xl.meta`
- `minio-backup/flower-shop/f475e459-b8ff-48fa-8546-ef32453d61ff_BO-004_04.jpg/a7220ad6-c302-406a-be23-5403ae39f5a3/part.1`
- `minio-backup/flower-shop/f475e459-b8ff-48fa-8546-ef32453d61ff_BO-004_04.jpg/xl.meta`
- `minio-backup/flower-shop/f5b5d1d1-0ad1-4b64-a9ad-fdb591c766a2_BO-002_01.jpg/aa1a02e6-2209-4a75-a070-4b9f7be81434/part.1`
- `minio-backup/flower-shop/f5b5d1d1-0ad1-4b64-a9ad-fdb591c766a2_BO-002_01.jpg/xl.meta`
- `minio-backup/flower-shop/f5fe521e-1d1e-4531-85a3-5c85116a93b5_/xl.meta`
- `minio-backup/flower-shop/fa64c56a-481c-4072-a414-cfd7b3dab44e_BO-002_04.jpg/91a58933-ce10-4d3f-b27c-e8ea627d40a3/part.1`
- `minio-backup/flower-shop/fa64c56a-481c-4072-a414-cfd7b3dab44e_BO-002_04.jpg/xl.meta`
- `minio-backup/flower-shop/ff615366-b2cf-4489-ae61-c93dd727783a_GIO-001_01.webp/xl.meta`
- `minio-backup.zip`
- `mvnw`
- `mvnw.cmd`
- `pom.xml`
- `README.md`
- `seed.sql`
- `src/main/java/com/souflow/config/AsyncConfig.java`
- `src/main/java/com/souflow/config/CacheWarmupConfig.java`
- `src/main/java/com/souflow/config/CorsConfig.java`
- `src/main/java/com/souflow/config/JacksonConfig.java`
- `src/main/java/com/souflow/config/LocaleConfig.java`
- `src/main/java/com/souflow/config/MdcFilter.java`
- `src/main/java/com/souflow/config/RateLimitInterceptor.java`
- `src/main/java/com/souflow/config/RedisConfig.java`
- `src/main/java/com/souflow/config/WebMvcConfig.java`
- `src/main/java/com/souflow/config/XssStringDeserializer.java`
- `src/main/java/com/souflow/exceptions/BusinessException.java`
- `src/main/java/com/souflow/exceptions/ForbiddenException.java`
- `src/main/java/com/souflow/exceptions/GlobalExceptionHandler.java`
- `src/main/java/com/souflow/FlowerShopApplication.java`
- `src/main/java/com/souflow/utils/LocaleUtil.java`
- `src/main/java/com/souflow/utils/StringUtil.java`
- `src/main/resources/application.properties`
- `src/test/java/com/souflow/BcryptTest.java`
- `src/test/java/com/souflow/FlowerShopApplicationTests.java`

