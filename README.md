## 🛒 GetIT - Mobile Marketplace App
GetIT adalah aplikasi marketplace berbasis Android yang dibangun menggunakan Jetpack Compose dan Firebase. Aplikasi ini memungkinkan pengguna untuk mencari produk, mengelola produk mereka sendiri, serta fitur khusus seperti verifikasi email instansi (ULM) dan integrasi cuaca.

DOKUMENTASI SINGKAT APLIKASI "GetIT"
UAS Pengembangan Aplikasi Mobile — Kelompok 7

A. Fitur Utama Aplikasi
Aplikasi GetIT memiliki 8 halaman utama aktif yang dikembangkan menggunakan Jetpack Compose dengan fungsi penuh sebagai berikut:  
* Autentikasi User Lokal: Registrasi dan login akun lokal khusus mahasiswa menggunakan email instansi (@mhs.ulm.ac.id) dengan sistem validasi tautan verifikasi via folder spam Gmail.  
* Marketplace C2C (BREAD): Pengelolaan katalog produk preloved secara dinamis, meliputi fungsi menjelajahi produk (Browse), melihat detail spesifikasi (Read), menambahkan produk baru (Add), memperbarui data (Edit), dan menghapus postingan (Delete).  
* Pop-Up Payment Information: Jendela informasi pembayaran internal yang menampilkan nomor rekening dan kontak WhatsApp penjual saat tombol beli ditekan.  
* Dashboard Integrasi Cuaca: Menampilkan informasi kondisi cuaca real-time wilayah Banjarmasin beserta indikator "Aman COD" untuk keamanan koordinasi transaksi di lingkungan kampus.  
* Riwayat Notifikasi Reaktif: Menyediakan pembaruan informasi otomatis secara real-time saat ada pengguna lain yang memasukkan barang milik penjual ke dalam keranjang.  
* Local Wishlist (Room): Fitur penyimpanan data offline untuk produk-produk favorit pengguna menggunakan database lokal.  
* Pengaturan & Fitur Tambahan: Pembagian blok menu yang bersih untuk manajemen profil, akun pembayaran, serta implementasi fitur Tema Gelap (Dark Theme) dan Multi-Bahasa (Localization) Inggris dan Indonesia.  

B. Penjelasan Arsitektur Aplikasi
GetIT menerapkan Clean Architecture dengan pola MVVM (Model-View-ViewModel) demi menjaga pemisahan tugas, stabilitas kode program, dan kemudahan pemeliharaan:  
* UI Layer (Presentation): Menggunakan Jetpack Compose untuk membangun tampilan deklaratif yang responsif, serta ViewModel dan StateFlow untuk menjaga retensi data (state retention) saat terjadi rotasi layar.  
* Domain Layer: Berisi struktur entitas data murni (Product, User, Category) dan interface GetItRepository sebagai kontrak bisnis logika aplikasi.  
* Data Layer: Implementasi data source yang memisahkan antara Firebase Realtime Database (cloud database), Room Database (local database), dan penyedia layanan jaringan.  

C. API Pihak Ketiga yang Digunakan
Aplikasi melakukan fetching data remote melalui koneksi jaringan menggunakan library Retrofit & OkHttp dari dua penyedia API eksternal:  
* Open-Meteo API: Digunakan untuk mengambil data remote kondisi cuaca berdasarkan koordinat geografis wilayah Banjarmasin secara berkala.  
* DummyJSON API: Digunakan untuk mengambil data katalog produk teknologi publik sebagai data produk tambahan di aplikasi.  

D. Struktur Folder Project
com.mobile.getit/
├── data/
│   ├── local/          # Room DB (Wishlist), PreferenceManager
│   ├── remote/         # Retrofit API Services (Weather, Product)
│   └── repository/     # Implementasi Repository (GetITRepositoryImpl)
├── domain/
│   ├── model/          # Data Class (User, Product, Weather)
│   └── repository/     # Interface Repository
├── ui/
│   ├── components/     # Reusable UI Components
│   ├── screen/         # Screen Composables (Login, Home, Profile, etc)
│   ├── theme/          # Color, Type, Theme definitions
│   └── viewmodel/      # Logic UI dan State Management
└── MainActivity.kt     # Entry point & Navigation setup

E. Cara Menjalankan Aplikasi
Prasyarat Sistem: Perangkat Android minimal SDK 24, Android Studio Iguana (atau versi lebih baru), JDK 17, serta koneksi internet aktif.  
Langkah-langkah Eksekusi:
1. Unduh dan ekstrak file proyek dari folder File project aplikasi mobile atau lakukan clone langsung melalui terminal dengan perintah:
	git clone [https://github.com/athayalailys/GetIT](https://github.com/athayalailys/GetIT)
2. Buka folder project tersebut menggunakan Android Studio, lalu tunggu hingga proses Gradle Sync selesai sepenuhnya.  
3. Hubungkan perangkat Android fisik menggunakan kabel data (pastikan USB Debugging aktif) atau gunakan Emulator Android Studio.  
4. Klik tombol Run 'app' (ikon segitiga hijau) pada toolbar atas Android Studio untuk melakukan build dan memasang aplikasi secara langsung.  
5. Alternatif instalasi cepat tanpa kompilasi kode dapat dilakukan dengan menyalin dan memasang file mentah .apk yang tersedia pada folder File APK atau hasil build aplikasi.

Kontributor:
Kelompok 7 - Athaya Laily Syafitri (2310817220008) dan Ahmad Luthfi Maulana (2410817310006)
Program Studi Teknologi Informasi, Universitas Lambung Mangkurat
