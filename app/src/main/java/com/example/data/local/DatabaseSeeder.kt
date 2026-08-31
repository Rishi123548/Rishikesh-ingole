package com.example.data.local

object DatabaseSeeder {

    suspend fun seedDatabase(database: SmartQuoteDatabase) {
        val settingsDao = database.settingsDao()
        val catalogDao = database.serviceCatalogDao()
        val packageDao = database.packageDao()
        val couponDao = database.couponDao()
        val customerDao = database.customerDao()
        val quotationDao = database.quotationDao()
        val invoiceDao = database.invoiceDao()
        val projectDao = database.projectDao()
        val activityDao = database.activityLogDao()

        // 1. Business Settings
        settingsDao.saveSettings(
            BusinessSettingsEntity(
                id = 1,
                businessName = "Rishi_dev",
                tagline = "AI GST Quotation, Billing & Project Requirement Builder",
                gstin = "27ALDPI8191C1Z5",
                pan = "ALDPI8191C",
                email = "rishiingole4@gmail.com",
                phone = "+91 98765 43210",
                address = "Tech Park, Hinjewadi Phase 1, Pune, Maharashtra 411057",
                state = "Maharashtra",
                stateCode = "27",
                country = "India",
                currencySymbol = "₹",
                defaultGstRate = 18.0,
                salesMaxAllowedDiscountPercent = 10.0,
                bankName = "HDFC Bank Ltd",
                accountHolder = "Rishi_dev Digital Solutions",
                accountNumber = "50200067891234",
                ifscCode = "HDFC0001234",
                upiId = "rishi.dev@okhdfcbank",
                termsAndConditions = "1. Quotation is valid for 15 days.\n2. Work starts after 30% advance payment.\n3. Additional requirements will be charged separately.\n4. Third-party API, domain & hosting charges billed at actuals.\n5. Project timeline starts after wireframes/requirements are confirmed.\n6. Final handover and source code release after 100% payment clearance."
            )
        )

        // 2. Service Catalog (Comprehensive Features)
        val services = listOf(
            // Website Pages & UI
            ServiceCatalogEntity(category = "Website", subCategory = "Pages", name = "1 Page (Landing Page)", description = "High-converting single page responsive website", basePrice = 4999.0, offerPrice = 3999.0, developmentDays = 2),
            ServiceCatalogEntity(category = "Website", subCategory = "Pages", name = "5 Pages Website", description = "Home, About, Services, Gallery, Contact", basePrice = 12000.0, offerPrice = 9999.0, developmentDays = 5),
            ServiceCatalogEntity(category = "Website", subCategory = "Pages", name = "10 Pages Website", description = "Comprehensive multi-page corporate web portal", basePrice = 22000.0, offerPrice = 18500.0, developmentDays = 8),
            ServiceCatalogEntity(category = "Website", subCategory = "Pages", name = "20 Pages Website", description = "Deep architecture website with custom sub-sections", basePrice = 40000.0, offerPrice = 34000.0, developmentDays = 14),
            ServiceCatalogEntity(category = "Website", subCategory = "Pages", name = "Unlimited / Custom Pages", description = "Dynamic CMS page generator with bespoke architecture", basePrice = 65000.0, offerPrice = 54999.0, developmentDays = 21),

            // UI/UX Design Tier
            ServiceCatalogEntity(category = "Website", subCategory = "Design Tier", name = "Basic UI", description = "Clean standard template based styling", basePrice = 3000.0, offerPrice = 2499.0, developmentDays = 1),
            ServiceCatalogEntity(category = "Website", subCategory = "Design Tier", name = "Standard UI", description = "Customized responsive layout with branding", basePrice = 5000.0, offerPrice = 4199.0, developmentDays = 2),
            ServiceCatalogEntity(category = "Website", subCategory = "Design Tier", name = "Premium UI", description = "Tailored bespoke modern typography and spacing", basePrice = 10000.0, offerPrice = 7999.0, developmentDays = 3),
            ServiceCatalogEntity(category = "Website", subCategory = "Design Tier", name = "Custom UI/UX (Figma to Code)", description = "Interactive prototype in Figma & pixel-perfect implementation", basePrice = 16000.0, offerPrice = 12999.0, developmentDays = 5),
            ServiceCatalogEntity(category = "Website", subCategory = "Design Tier", name = "Animated UI & Micro-interactions", description = "Smooth GSAP/Framer web animations and transitions", basePrice = 14000.0, offerPrice = 11500.0, developmentDays = 4),
            ServiceCatalogEntity(category = "Website", subCategory = "Design Tier", name = "3D Interactive Experience", description = "Three.js / WebGL 3D models and interactive canvas", basePrice = 25000.0, offerPrice = 19999.0, developmentDays = 7),

            // Core Website Features
            ServiceCatalogEntity(category = "Website", subCategory = "Core Features", name = "Login & Registration", description = "Secure email and password user authentication", basePrice = 4000.0, offerPrice = 3200.0, developmentDays = 2),
            ServiceCatalogEntity(category = "Website", subCategory = "Core Features", name = "OTP Login (SMS/WhatsApp)", description = "Instant mobile verification code system", basePrice = 4500.0, offerPrice = 3800.0, developmentDays = 2),
            ServiceCatalogEntity(category = "Website", subCategory = "Core Features", name = "Google & Social Login", description = "1-click OAuth authentication integration", basePrice = 3500.0, offerPrice = 2900.0, developmentDays = 1),
            ServiceCatalogEntity(category = "Website", subCategory = "Core Features", name = "User Dashboard", description = "Client profile, order status, downloads & settings", basePrice = 8000.0, offerPrice = 6500.0, developmentDays = 3),
            ServiceCatalogEntity(category = "Website", subCategory = "Core Features", name = "Admin Dashboard", description = "Master control panel to manage content, leads & orders", basePrice = 12000.0, offerPrice = 9500.0, developmentDays = 4),
            ServiceCatalogEntity(category = "Website", subCategory = "Core Features", name = "Contact & Lead Form", description = "Instant email/telegram lead notifications", basePrice = 2000.0, offerPrice = 1500.0, developmentDays = 1),
            ServiceCatalogEntity(category = "Website", subCategory = "Core Features", name = "Booking & Appointment Calendar", description = "Real-time time-slot reservation with reminder sync", basePrice = 9000.0, offerPrice = 7500.0, developmentDays = 4),
            ServiceCatalogEntity(category = "Website", subCategory = "Core Features", name = "Payment Gateway Integration", description = "Razorpay, Cashfree, Stripe, UPI payments", basePrice = 6000.0, offerPrice = 4800.0, developmentDays = 2),
            ServiceCatalogEntity(category = "Website", subCategory = "Core Features", name = "UPI Instant QR Payment", description = "Dynamic QR code generator for direct UPI transfer", basePrice = 3500.0, offerPrice = 2800.0, developmentDays = 1),
            ServiceCatalogEntity(category = "Website", subCategory = "Core Features", name = "WhatsApp Chat & Floating Button", description = "Direct customer WhatsApp connect with prefilled message", basePrice = 1800.0, offerPrice = 1400.0, developmentDays = 1),
            ServiceCatalogEntity(category = "Website", subCategory = "Core Features", name = "AI Chatbot Assistant", description = "Trained smart bot answering FAQs and capturing leads", basePrice = 12000.0, offerPrice = 8999.0, developmentDays = 4),
            ServiceCatalogEntity(category = "Website", subCategory = "Core Features", name = "Push & Email Notifications", description = "Automated transactional emails and browser push", basePrice = 4500.0, offerPrice = 3600.0, developmentDays = 2),
            ServiceCatalogEntity(category = "Website", subCategory = "Core Features", name = "Google Maps & Business Integration", description = "Interactive map, route directions, Google reviews widget", basePrice = 2500.0, offerPrice = 1999.0, developmentDays = 1),
            ServiceCatalogEntity(category = "Website", subCategory = "Core Features", name = "SEO Optimization Suite", description = "Meta tags, schema markup, sitemap & search indexing", basePrice = 6000.0, offerPrice = 4500.0, developmentDays = 2),
            ServiceCatalogEntity(category = "Website", subCategory = "Core Features", name = "Multi-Language & Dark Mode", description = "Localization and dynamic theme switcher", basePrice = 5500.0, offerPrice = 4200.0, developmentDays = 2),
            ServiceCatalogEntity(category = "Website", subCategory = "Core Features", name = "Advanced Search & Custom Filters", description = "Faceted search with multi-parameter filter engine", basePrice = 7000.0, offerPrice = 5500.0, developmentDays = 3),

            // E-Commerce Module
            ServiceCatalogEntity(category = "E-Commerce", subCategory = "Catalog & Shop", name = "Product & Variant Management", description = "Size, color, SKU, inventory stock and image gallery", basePrice = 9000.0, offerPrice = 7500.0, developmentDays = 3),
            ServiceCatalogEntity(category = "E-Commerce", subCategory = "Catalog & Shop", name = "Shopping Cart & Wishlist", description = "Persistent shopping bag with quick slide drawer", basePrice = 5000.0, offerPrice = 3999.0, developmentDays = 2),
            ServiceCatalogEntity(category = "E-Commerce", subCategory = "Catalog & Shop", name = "Coupon & Promo Engine", description = "Percentage, flat, min-order and festival coupons", basePrice = 6000.0, offerPrice = 4800.0, developmentDays = 2),
            ServiceCatalogEntity(category = "E-Commerce", subCategory = "Catalog & Shop", name = "Order Management & Tracking", description = "Full order lifecycle, shipping status and dispatch logs", basePrice = 10000.0, offerPrice = 8200.0, developmentDays = 4),
            ServiceCatalogEntity(category = "E-Commerce", subCategory = "Catalog & Shop", name = "Automated GST Invoicing", description = "Instant downloadable PDF tax invoices for customers", basePrice = 7500.0, offerPrice = 5900.0, developmentDays = 2),
            ServiceCatalogEntity(category = "E-Commerce", subCategory = "Catalog & Shop", name = "Shipping API (Shiprocket/Delhivery)", description = "Auto-calculate courier charges and generate airway bills", basePrice = 8500.0, offerPrice = 6900.0, developmentDays = 3),
            ServiceCatalogEntity(category = "E-Commerce", subCategory = "Catalog & Shop", name = "Reviews, Ratings & COD Option", description = "Customer feedback with photo uploads and Cash-on-delivery", basePrice = 5000.0, offerPrice = 3900.0, developmentDays = 2),

            // Restaurant Module
            ServiceCatalogEntity(category = "Restaurant", subCategory = "Food Ordering", name = "Digital QR Menu System", description = "Contactless table menu scanning with categorized food items", basePrice = 6000.0, offerPrice = 4999.0, developmentDays = 2),
            ServiceCatalogEntity(category = "Restaurant", subCategory = "Food Ordering", name = "Online Food Ordering & Table Booking", description = "Dine-in booking and doorstep delivery cart", basePrice = 14000.0, offerPrice = 11500.0, developmentDays = 4),
            ServiceCatalogEntity(category = "Restaurant", subCategory = "Food Ordering", name = "Live Kitchen Dashboard (KDS)", description = "Real-time kitchen order ticket (KOT) status screen", basePrice = 11000.0, offerPrice = 8900.0, developmentDays = 4),
            ServiceCatalogEntity(category = "Restaurant", subCategory = "Food Ordering", name = "WhatsApp Direct Ordering Bot", description = "Customers order directly inside WhatsApp chat", basePrice = 9500.0, offerPrice = 7800.0, developmentDays = 3),

            // Real Estate Module
            ServiceCatalogEntity(category = "Real Estate", subCategory = "Property CRM", name = "Property Listing & Advanced Map Search", description = "Filters for BHK, budget, location and interactive map pins", basePrice = 12000.0, offerPrice = 9800.0, developmentDays = 4),
            ServiceCatalogEntity(category = "Real Estate", subCategory = "Property CRM", name = "360° Property Tour & Video Showcase", description = "Virtual panoramic walkthrough embedder", basePrice = 8000.0, offerPrice = 6500.0, developmentDays = 3),
            ServiceCatalogEntity(category = "Real Estate", subCategory = "Property CRM", name = "Site Visit Booking & Agent CRM", description = "Lead assignment, follow-up calendar and caller dashboard", basePrice = 15000.0, offerPrice = 12500.0, developmentDays = 5),
            ServiceCatalogEntity(category = "Real Estate", subCategory = "Property CRM", name = "AI Real Estate Calling & WhatsApp Follow-up", description = "Automated voice/chat lead qualification agent", basePrice = 20000.0, offerPrice = 16500.0, developmentDays = 6),

            // Mobile App Development
            ServiceCatalogEntity(category = "Mobile App", subCategory = "Platform", name = "Android Native / Flutter App", description = "Google Play store ready modern Android application", basePrice = 30000.0, offerPrice = 24999.0, developmentDays = 15),
            ServiceCatalogEntity(category = "Mobile App", subCategory = "Platform", name = "iOS App (Apple Swift/Flutter)", description = "Apple App Store compliant iOS application", basePrice = 35000.0, offerPrice = 29999.0, developmentDays = 16),
            ServiceCatalogEntity(category = "Mobile App", subCategory = "Platform", name = "Android + iOS Dual Platform", description = "Cross-platform dual deployment with shared backend", basePrice = 50000.0, offerPrice = 42500.0, developmentDays = 22),

            // Mobile App Screens UI
            ServiceCatalogEntity(category = "Mobile App", subCategory = "App Screens UI", name = "1–5 Screens UI", description = "Essential onboarding, auth & home flow", basePrice = 6000.0, offerPrice = 4999.0, developmentDays = 3),
            ServiceCatalogEntity(category = "Mobile App", subCategory = "App Screens UI", name = "6–10 Screens UI", description = "Standard complete application navigation", basePrice = 11000.0, offerPrice = 8999.0, developmentDays = 5),
            ServiceCatalogEntity(category = "Mobile App", subCategory = "App Screens UI", name = "11–20 Screens UI", description = "Rich feature suite with dedicated sub-flows", basePrice = 18000.0, offerPrice = 14999.0, developmentDays = 8),
            ServiceCatalogEntity(category = "Mobile App", subCategory = "App Screens UI", name = "21–30 Screens UI", description = "Complex enterprise marketplace app hierarchy", basePrice = 26000.0, offerPrice = 21999.0, developmentDays = 12),
            ServiceCatalogEntity(category = "Mobile App", subCategory = "App Screens UI", name = "30+ Screens (Custom UI/UX)", description = "Comprehensive full-stack bespoke app ecosystem", basePrice = 38000.0, offerPrice = 31999.0, developmentDays = 18),

            // Mobile App Features
            ServiceCatalogEntity(category = "Mobile App", subCategory = "App Features", name = "GPS & Live Location Tracking", description = "Real-time delivery partner & customer coordinate sync", basePrice = 12000.0, offerPrice = 9800.0, developmentDays = 4),
            ServiceCatalogEntity(category = "Mobile App", subCategory = "App Features", name = "In-App Wallet & Subscription Engine", description = "Prepaid wallet, recurring monthly/yearly plans", basePrice = 10000.0, offerPrice = 8500.0, developmentDays = 4),
            ServiceCatalogEntity(category = "Mobile App", subCategory = "App Features", name = "1-to-1 In-App Live Chat", description = "Real-time websocket/firebase messaging and media exchange", basePrice = 9000.0, offerPrice = 7500.0, developmentDays = 3),
            ServiceCatalogEntity(category = "Mobile App", subCategory = "App Features", name = "Camera, QR & Barcode Scanner", description = "Instant camera capture, QR verification and barcode reader", basePrice = 6500.0, offerPrice = 5200.0, developmentDays = 2),
            ServiceCatalogEntity(category = "Mobile App", subCategory = "App Features", name = "Offline Database Sync", description = "Local SQLite caching with background server auto-sync", basePrice = 8500.0, offerPrice = 6900.0, developmentDays = 3),

            // Software Development
            ServiceCatalogEntity(category = "Software", subCategory = "Business Suite", name = "GST Billing & Invoicing Software", description = "Desktop/Web GST tax invoice generator with GSTR-1 export", basePrice = 18000.0, offerPrice = 14999.0, developmentDays = 7),
            ServiceCatalogEntity(category = "Software", subCategory = "Business Suite", name = "Point of Sale (POS) Software", description = "Barcode thermal printing, quick billing, daily cash reconciliation", basePrice = 22000.0, offerPrice = 17999.0, developmentDays = 8),
            ServiceCatalogEntity(category = "Software", subCategory = "Business Suite", name = "Gym Management Software", description = "Member attendance, biometric sync, fee reminders, plan renewals", basePrice = 20000.0, offerPrice = 16500.0, developmentDays = 7),
            ServiceCatalogEntity(category = "Software", subCategory = "Business Suite", name = "Restaurant & Hotel Management Software", description = "Table booking, room status, billing, kitchen orders", basePrice = 28000.0, offerPrice = 23500.0, developmentDays = 10),
            ServiceCatalogEntity(category = "Software", subCategory = "Business Suite", name = "Sales CRM & Lead Pipeline", description = "Deal stage kanban, salesperson tracking, follow-up logs", basePrice = 24000.0, offerPrice = 19500.0, developmentDays = 9),
            ServiceCatalogEntity(category = "Software", subCategory = "Business Suite", name = "Inventory & Warehouse Stock Software", description = "Low stock alert, batch expiry, supplier purchase orders", basePrice = 25000.0, offerPrice = 20999.0, developmentDays = 9),
            ServiceCatalogEntity(category = "Software", subCategory = "Business Suite", name = "Employee Attendance & Payroll Software", description = "Salary slips, leaves, shifts, PF/ESIC tax breakdown", basePrice = 26000.0, offerPrice = 21500.0, developmentDays = 10),
            ServiceCatalogEntity(category = "Software", subCategory = "Business Suite", name = "Custom ERP Enterprise Software", description = "Multi-branch modular enterprise resource planning system", basePrice = 60000.0, offerPrice = 49999.0, developmentDays = 25),

            // AI Automation
            ServiceCatalogEntity(category = "AI Automation", subCategory = "Intelligent Agents", name = "AI Chatbot for Website / WhatsApp", description = "24/7 autonomous lead capture, FAQs, multi-lingual support", basePrice = 14000.0, offerPrice = 10999.0, developmentDays = 4),
            ServiceCatalogEntity(category = "AI Automation", subCategory = "Intelligent Agents", name = "AI Voice Calling Agent", description = "Ultra-realistic outbound/inbound phone calls for booking & leads", basePrice = 25000.0, offerPrice = 19999.0, developmentDays = 7),
            ServiceCatalogEntity(category = "AI Automation", subCategory = "Intelligent Agents", name = "Lead Generation & Outreach Bot", description = "Auto-scrapes, verifies leads, and sends personalized sequences", basePrice = 16000.0, offerPrice = 12999.0, developmentDays = 5),
            ServiceCatalogEntity(category = "AI Automation", subCategory = "Intelligent Agents", name = "WhatsApp Business API Automation", description = "Green-tick verified broadcast, drip marketing, order alerts", basePrice = 12000.0, offerPrice = 9500.0, developmentDays = 3),
            ServiceCatalogEntity(category = "AI Automation", subCategory = "Intelligent Agents", name = "AI Appointment & Booking Agent", description = "Conversational slot scheduling with Google Calendar sync", basePrice = 15000.0, offerPrice = 11999.0, developmentDays = 4),
            ServiceCatalogEntity(category = "AI Automation", subCategory = "Intelligent Agents", name = "AI Content & Marketing Generator", description = "Automated social media posts, blog articles, SEO copy", basePrice = 10000.0, offerPrice = 7999.0, developmentDays = 3),
            ServiceCatalogEntity(category = "AI Automation", subCategory = "Intelligent Agents", name = "Custom AI Workflow & Model Tuning", description = "Customized LLM fine-tuning on company data and CRM sync", basePrice = 35000.0, offerPrice = 28999.0, developmentDays = 12),

            // Add-On Services
            ServiceCatalogEntity(category = "Add-on", subCategory = "Hosting & Infra", name = "Domain Name (.com / .in / .co.in)", description = "1 Year top-level domain registration & DNS setup", basePrice = 1500.0, offerPrice = 1200.0, developmentDays = 1),
            ServiceCatalogEntity(category = "Add-on", subCategory = "Hosting & Infra", name = "High-Speed SSD Cloud Hosting (1 Year)", description = "99.9% uptime, cPanel, daily backup, LiteSpeed server", basePrice = 4500.0, offerPrice = 3499.0, developmentDays = 1),
            ServiceCatalogEntity(category = "Add-on", subCategory = "Hosting & Infra", name = "SSL Certificate & Security Hardening", description = "HTTPS lock, DDoS protection, firewall rules", basePrice = 2000.0, offerPrice = 1499.0, developmentDays = 1),
            ServiceCatalogEntity(category = "Add-on", subCategory = "Hosting & Infra", name = "Corporate Business Email (5 Inboxes)", description = "Professional info@yourdomain.com with Webmail/Outlook", basePrice = 2500.0, offerPrice = 1999.0, developmentDays = 1),
            ServiceCatalogEntity(category = "Add-on", subCategory = "Branding & Creative", name = "Professional Logo & Brand Kit Design", description = "3 Concepts, vector source files, business card & letterhead", basePrice = 5000.0, offerPrice = 3999.0, developmentDays = 3),
            ServiceCatalogEntity(category = "Add-on", subCategory = "Branding & Creative", name = "Annual Maintenance Contract (AMC)", description = "1 Year bug fixes, security updates & monthly content changes", basePrice = 12000.0, offerPrice = 9999.0, developmentDays = 1),
            ServiceCatalogEntity(category = "Add-on", subCategory = "Marketing & APIs", name = "Google Business Profile & Maps Setup", description = "Verification, keyword optimization, local SEO ranking", basePrice = 3500.0, offerPrice = 2499.0, developmentDays = 2),
            ServiceCatalogEntity(category = "Add-on", subCategory = "Marketing & APIs", name = "Payment Gateway Setup & KYC Clearance", description = "Merchant account onboarding with lowest transaction fee", basePrice = 4000.0, offerPrice = 2999.0, developmentDays = 2),
            ServiceCatalogEntity(category = "Add-on", subCategory = "Marketing & APIs", name = "Google Play Console Publishing", description = "Store listing, privacy policy, APK signing and approval", basePrice = 3000.0, offerPrice = 2199.0, developmentDays = 2),
            ServiceCatalogEntity(category = "Add-on", subCategory = "Marketing & APIs", name = "Apple App Store Publishing", description = "TestFlight distribution, metadata, App Store review assist", basePrice = 5000.0, offerPrice = 3999.0, developmentDays = 3)
        )
        catalogDao.insertServices(services)

        // 3. Bundled Packages
        val packages = listOf(
            PackageEntity(
                name = "STARTUP DIGITAL LAUNCH",
                description = "5 Pages Website + Logo Design + Google Business Profile + WhatsApp Integration + 1 Yr Hosting & Domain",
                regularPrice = 30000.0,
                packagePrice = 19999.0,
                includedFeatureIdsJson = "Website,Logo,Google Business,WhatsApp,Hosting",
                tag = "MOST POPULAR"
            ),
            PackageEntity(
                name = "E-COMMERCE PRO PACKAGE",
                description = "E-Commerce Store + Product Management + Payment Gateway + WhatsApp Cart + Invoice Generator + 1 Yr Server",
                regularPrice = 45000.0,
                packagePrice = 31999.0,
                includedFeatureIdsJson = "E-Commerce,Payment Gateway,WhatsApp,Invoice,Server",
                tag = "HIGH ROI"
            ),
            PackageEntity(
                name = "AI AUTOMATION & CALLING SUITE",
                description = "AI Voice Calling Agent + WhatsApp AI Chatbot + Lead CRM Pipeline + Automated Meeting Booking",
                regularPrice = 65000.0,
                packagePrice = 46999.0,
                includedFeatureIdsJson = "AI Calling,WhatsApp Bot,CRM,Booking",
                tag = "SMART TECH"
            )
        )
        packageDao.insertPackages(packages)

        // 4. Coupons
        val coupons = listOf(
            CouponEntity(code = "RISHI10", discountType = "PERCENTAGE", discountValue = 10.0, minOrderValue = 10000.0, maxDiscountValue = 10000.0),
            CouponEntity(code = "DEV20", discountType = "PERCENTAGE", discountValue = 20.0, minOrderValue = 30000.0, maxDiscountValue = 15000.0),
            CouponEntity(code = "NEWCLIENT", discountType = "FLAT", discountValue = 2500.0, minOrderValue = 15000.0, maxDiscountValue = 2500.0),
            CouponEntity(code = "APP5000", discountType = "FLAT", discountValue = 5000.0, minOrderValue = 35000.0, maxDiscountValue = 5000.0)
        )
        couponDao.insertCoupons(coupons)

        // 5. Pre-seeded Customers
        val customer1Id = customerDao.insertCustomer(
            CustomerEntity(
                name = "Amit Sharma",
                businessName = "Apex Retailers & Mart",
                mobileNumber = "+91 98230 11223",
                whatsAppNumber = "+91 98230 11223",
                email = "amit.sharma@apexretail.in",
                billingAddress = "Plot 44, Market Yard, Swargate",
                city = "Pune",
                state = "Maharashtra",
                pinCode = "411037",
                gstin = "27AAACA1234F1Z9",
                pan = "AAACA1234F",
                customerType = "Retail Store"
            )
        )

        val customer2Id = customerDao.insertCustomer(
            CustomerEntity(
                name = "Pooja Deshmukh",
                businessName = "Spice & Flavors Bistro",
                mobileNumber = "+91 97654 88990",
                whatsAppNumber = "+91 97654 88990",
                email = "contact@spicenflavors.com",
                billingAddress = "Shop 12, Baner High Street",
                city = "Pune",
                state = "Maharashtra",
                pinCode = "411045",
                gstin = "27BZXPD9912K1Z4",
                pan = "BZXPD9912K",
                customerType = "Restaurant"
            )
        )

        val customer3Id = customerDao.insertCustomer(
            CustomerEntity(
                name = "Vikram Malhotra",
                businessName = "Skyline Realty Projects",
                mobileNumber = "+91 99100 44332",
                whatsAppNumber = "+91 99100 44332",
                email = "vikram@skylinerealty.in",
                billingAddress = "Tower B, BKC Complex",
                city = "Mumbai",
                state = "Maharashtra",
                pinCode = "400051",
                gstin = "27CLAPM4321R1Z1",
                pan = "CLAPM4321R",
                customerType = "Real Estate"
            )
        )

        // 6. Pre-seeded Sample Quotation 1 (Accepted -> Project & Invoice)
        val quote1Id = quotationDao.insertQuotation(
            QuotationEntity(
                quotationNumber = "RD-QT-2026-001",
                customerId = customer1Id,
                customerName = "Amit Sharma (Apex Retailers)",
                customerPhone = "+91 98230 11223",
                customerEmail = "amit.sharma@apexretail.in",
                projectName = "Apex E-Commerce Portal & Android App",
                projectType = "E-Commerce Website & Android App",
                status = "Converted to Invoice",
                subtotal = 55000.0,
                offerDiscount = 8000.0,
                couponCode = "RISHI10",
                couponDiscount = 4700.0,
                taxableAmount = 42300.0,
                gstType = "CGST_SGST",
                gstRate = 18.0,
                cgstAmount = 3807.0,
                sgstAmount = 3807.0,
                grandTotal = 49914.0,
                totalSavings = 12700.0,
                termsAndConditions = "1. 30% advance on signing.\n2. Delivery in 20 working days."
            )
        )

        quotationDao.insertQuotationItems(
            listOf(
                QuotationItemEntity(quotationId = quote1Id, serviceName = "E-Commerce Website (10 Pages)", originalPrice = 22000.0, offerPrice = 18500.0, taxableAmount = 18500.0, totalAmount = 21830.0),
                QuotationItemEntity(quotationId = quote1Id, serviceName = "Android Mobile App", originalPrice = 30000.0, offerPrice = 24999.0, taxableAmount = 24999.0, totalAmount = 29498.82),
                QuotationItemEntity(quotationId = quote1Id, serviceName = "Payment Gateway & UPI Setup", originalPrice = 6000.0, offerPrice = 4800.0, taxableAmount = 4800.0, totalAmount = 5664.0),
                QuotationItemEntity(quotationId = quote1Id, serviceName = "WhatsApp Business Integration", originalPrice = 1800.0, offerPrice = 1400.0, taxableAmount = 1400.0, totalAmount = 1652.0)
            )
        )

        // 7. Pre-seeded Invoice for Quote 1
        val inv1Id = invoiceDao.insertInvoice(
            InvoiceEntity(
                invoiceNumber = "RD-INV-2026-001",
                quotationId = quote1Id,
                customerId = customer1Id,
                customerName = "Amit Sharma",
                customerBusiness = "Apex Retailers & Mart",
                customerGstin = "27AAACA1234F1Z9",
                customerAddress = "Plot 44, Market Yard, Pune, Maharashtra 411037",
                projectName = "Apex E-Commerce Portal & Android App",
                invoiceType = "GST Tax Invoice",
                status = "Partially Paid",
                subtotal = 55000.0,
                discount = 12700.0,
                taxableTotal = 42300.0,
                gstType = "CGST_SGST",
                gstRate = 18.0,
                cgstAmount = 3807.0,
                sgstAmount = 3807.0,
                totalGst = 7614.0,
                roundOff = 0.0,
                grandTotal = 49914.0,
                paidAmount = 15000.0,
                dueAmount = 34914.0,
                amountInWords = "Rupees Forty-Nine Thousand Nine Hundred Fourteen Only"
            )
        )

        invoiceDao.insertInvoiceItems(
            listOf(
                InvoiceItemEntity(invoiceId = inv1Id, itemName = "E-Commerce Website & Android App Development", description = "Full stack web portal with native Android application", rate = 42300.0, taxableAmount = 42300.0, cgstAmount = 3807.0, sgstAmount = 3807.0, total = 49914.0)
            )
        )

        database.paymentDao().insertPayment(
            PaymentEntity(
                invoiceId = inv1Id,
                amount = 15000.0,
                paymentType = "Advance Payment (30%)",
                paymentMethod = "UPI",
                transactionRef = "UPI/2026/89823192",
                notes = "Booking advance received via UPI QR"
            )
        )

        // 8. Pre-seeded Active Project
        val proj1Id = projectDao.insertProject(
            ProjectEntity(
                quotationId = quote1Id,
                invoiceId = inv1Id,
                customerId = customer1Id,
                customerName = "Amit Sharma (Apex Retailers)",
                projectName = "Apex E-Commerce Portal & Android App",
                projectType = "E-Commerce Website & Android App",
                stage = "Development Started",
                progressPercent = 45,
                totalBudget = 49914.0,
                receivedAmount = 15000.0,
                isCompleted = false
            )
        )

        projectDao.insertProjectFeatures(
            listOf(
                ProjectFeatureEntity(projectId = proj1Id, featureName = "UI/UX Design & Wireframing", status = "Completed"),
                ProjectFeatureEntity(projectId = proj1Id, featureName = "Product Catalog & Cart", status = "Completed"),
                ProjectFeatureEntity(projectId = proj1Id, featureName = "Payment Gateway Integration", status = "In Progress"),
                ProjectFeatureEntity(projectId = proj1Id, featureName = "Android App Core Screens", status = "In Progress"),
                ProjectFeatureEntity(projectId = proj1Id, featureName = "WhatsApp Order Alerts", status = "Not Started"),
                ProjectFeatureEntity(projectId = proj1Id, featureName = "Final QA & Play Store Deployment", status = "Not Started")
            )
        )

        // 9. Pre-seeded Quotation 2 (Pending Review for Pooja Deshmukh)
        val quote2Id = quotationDao.insertQuotation(
            QuotationEntity(
                quotationNumber = "RD-QT-2026-002",
                customerId = customer2Id,
                customerName = "Pooja Deshmukh (Spice & Flavors)",
                customerPhone = "+91 97654 88990",
                customerEmail = "contact@spicenflavors.com",
                projectName = "Spice & Flavors QR Menu & Food Order System",
                projectType = "Restaurant Food Ordering System",
                status = "Sent",
                subtotal = 38000.0,
                offerDiscount = 6000.0,
                couponCode = "NEWCLIENT",
                couponDiscount = 2500.0,
                taxableAmount = 29500.0,
                gstType = "CGST_SGST",
                gstRate = 18.0,
                cgstAmount = 2655.0,
                sgstAmount = 2655.0,
                grandTotal = 34810.0,
                totalSavings = 8500.0
            )
        )

        quotationDao.insertQuotationItems(
            listOf(
                QuotationItemEntity(quotationId = quote2Id, serviceName = "Digital QR Menu System", originalPrice = 6000.0, offerPrice = 4999.0, taxableAmount = 4999.0, totalAmount = 5898.82),
                QuotationItemEntity(quotationId = quote2Id, serviceName = "Online Food Ordering & Table Booking", originalPrice = 14000.0, offerPrice = 11500.0, taxableAmount = 11500.0, totalAmount = 13570.0),
                QuotationItemEntity(quotationId = quote2Id, serviceName = "Kitchen Dashboard (KDS)", originalPrice = 11000.0, offerPrice = 8900.0, taxableAmount = 8900.0, totalAmount = 10502.0),
                QuotationItemEntity(quotationId = quote2Id, serviceName = "WhatsApp Ordering Bot", originalPrice = 9500.0, offerPrice = 7800.0, taxableAmount = 7800.0, totalAmount = 9204.0)
            )
        )

        // Activity log
        activityDao.insertLog(
            ActivityLogEntity(
                actionType = "SYSTEM_INITIALIZED",
                title = "SmartQuote Pro Ready",
                description = "Master service catalog and business settings loaded for Rishi_dev."
            )
        )
    }
}
