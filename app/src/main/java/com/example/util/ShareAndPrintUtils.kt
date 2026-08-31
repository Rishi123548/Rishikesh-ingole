package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import com.example.data.local.*

object ShareAndPrintUtils {

    fun generateQuotationHtml(
        quote: QuotationEntity,
        items: List<QuotationItemEntity>,
        settings: BusinessSettingsEntity
    ): String {
        val rows = items.mapIndexed { idx, item ->
            """
            <tr>
                <td style="text-align:center; padding: 8px; border: 1px solid #cbd5e1;">${idx + 1}</td>
                <td style="padding: 8px; border: 1px solid #cbd5e1;">
                    <strong>${item.serviceName}</strong>
                    ${if (item.description.isNotEmpty()) "<br/><span style='font-size:11px; color:#64748b;'>${item.description}</span>" else ""}
                </td>
                <td style="text-align:center; padding: 8px; border: 1px solid #cbd5e1;">${item.quantity}</td>
                <td style="text-align:right; padding: 8px; border: 1px solid #cbd5e1;">${CurrencyUtils.formatInr(item.originalPrice)}</td>
                <td style="text-align:right; padding: 8px; border: 1px solid #cbd5e1; color:#059669;">${if (item.discount > 0) CurrencyUtils.formatInr(item.discount) else "-"}</td>
                <td style="text-align:right; padding: 8px; border: 1px solid #cbd5e1;"><strong>${CurrencyUtils.formatInr(item.taxableAmount)}</strong></td>
                <td style="text-align:right; padding: 8px; border: 1px solid #cbd5e1;"><strong>${CurrencyUtils.formatInr(item.totalAmount)}</strong></td>
            </tr>
            """.trimIndent()
        }.joinToString("\n")

        val gstRows = if (quote.gstType == "CGST_SGST") {
            """
            <tr>
                <td style="padding: 6px; color: #475569;">CGST @ ${(quote.gstRate / 2)}%:</td>
                <td style="padding: 6px; text-align:right; font-weight:600;">${CurrencyUtils.formatInr(quote.cgstAmount)}</td>
            </tr>
            <tr>
                <td style="padding: 6px; color: #475569;">SGST @ ${(quote.gstRate / 2)}%:</td>
                <td style="padding: 6px; text-align:right; font-weight:600;">${CurrencyUtils.formatInr(quote.sgstAmount)}</td>
            </tr>
            """.trimIndent()
        } else {
            """
            <tr>
                <td style="padding: 6px; color: #475569;">IGST @ ${quote.gstRate}%:</td>
                <td style="padding: 6px; text-align:right; font-weight:600;">${CurrencyUtils.formatInr(quote.igstAmount)}</td>
            </tr>
            """.trimIndent()
        }

        return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="utf-8"/>
            <title>Quotation - ${quote.quotationNumber}</title>
            <style>
                body { font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; color: #1e293b; padding: 24px; font-size: 13px; line-height: 1.5; }
                .header { display: flex; justify-content: space-between; border-bottom: 2px solid #2563eb; padding-bottom: 16px; margin-bottom: 20px; }
                .logo-title { font-size: 24px; font-weight: bold; color: #1e3a8a; }
                .badge { background: #dbeafe; color: #1e40af; padding: 4px 10px; border-radius: 4px; font-weight: bold; display: inline-block; font-size: 12px; }
                .info-grid { display: flex; justify-content: space-between; margin-bottom: 24px; }
                .info-box { width: 48%; }
                table { width: 100%; border-collapse: collapse; margin-bottom: 20px; }
                th { background-color: #f1f5f9; color: #1e293b; font-weight: 700; padding: 10px; border: 1px solid #cbd5e1; font-size: 12px; }
                .summary-table { width: 320px; margin-left: auto; border-collapse: collapse; }
                .grand-total-row { background-color: #eff6ff; border-top: 2px solid #2563eb; border-bottom: 2px solid #2563eb; }
                .savings-pill { background-color: #dcfce7; color: #15803d; padding: 6px 12px; border-radius: 6px; font-weight: bold; display: inline-block; }
                .footer { margin-top: 30px; border-top: 1px solid #e2e8f0; padding-top: 16px; font-size: 11px; color: #64748b; }
            </style>
        </head>
        <body>
            <div class="header">
                <div>
                    <div class="logo-title">${settings.businessName}</div>
                    <div style="color: #475569; font-size: 12px;">${settings.tagline}</div>
                    <div style="margin-top: 6px; font-size: 12px;"><strong>GSTIN:</strong> ${settings.gstin} | <strong>PAN:</strong> ${settings.pan}</div>
                    <div style="font-size: 12px;">${settings.email} | ${settings.phone}</div>
                </div>
                <div style="text-align: right;">
                    <div class="badge">PROJECT ESTIMATION</div>
                    <h2 style="margin: 6px 0 0 0; color: #0f172a;">${quote.quotationNumber}</h2>
                    <div style="color: #64748b; font-size: 12px;">Date: ${CurrencyUtils.formatDate(quote.createdAt)}</div>
                    <div style="color: #ef4444; font-size: 12px; font-weight: 600;">Valid Until: ${CurrencyUtils.formatDate(quote.validUntil)}</div>
                </div>
            </div>

            <div class="info-grid">
                <div class="info-box">
                    <strong style="color: #475569; text-transform: uppercase; font-size: 11px;">Quotation Prepared For:</strong>
                    <div style="font-size: 15px; font-weight: bold; color: #0f172a; margin-top: 4px;">${quote.customerName}</div>
                    <div>Phone: ${quote.customerPhone}</div>
                    ${if (quote.customerEmail.isNotEmpty()) "<div>Email: ${quote.customerEmail}</div>" else ""}
                </div>
                <div class="info-box" style="text-align: right;">
                    <strong style="color: #475569; text-transform: uppercase; font-size: 11px;">Project Scope:</strong>
                    <div style="font-size: 15px; font-weight: bold; color: #2563eb; margin-top: 4px;">${quote.projectName}</div>
                    <div style="color: #475569;">Type: ${quote.projectType}</div>
                    <div style="margin-top: 4px;"><span class="badge" style="background:#fef3c7; color:#92400e;">Status: ${quote.status}</span></div>
                </div>
            </div>

            <table>
                <thead>
                    <tr>
                        <th style="width: 5%;">#</th>
                        <th style="width: 40%; text-align: left;">Requirement / Module</th>
                        <th style="width: 8%;">Qty</th>
                        <th style="width: 15%; text-align: right;">Base Price</th>
                        <th style="width: 12%; text-align: right;">Discount</th>
                        <th style="width: 15%; text-align: right;">Taxable Amount</th>
                        <th style="width: 15%; text-align: right;">Total (+GST)</th>
                    </tr>
                </thead>
                <tbody>
                    $rows
                </tbody>
            </table>

            <div style="display: flex; justify-content: space-between; align-items: flex-start;">
                <div style="width: 50%;">
                    ${if (quote.totalSavings > 0) """
                        <div class="savings-pill">
                            🎉 Total Client Savings: ${CurrencyUtils.formatInr(quote.totalSavings)}
                        </div>
                    """.trimIndent() else ""}
                    
                    <div style="margin-top: 14px;">
                        <strong>Bank & UPI Details:</strong>
                        <div style="font-size: 12px; color:#475569; margin-top:4px;">
                            Bank: ${settings.bankName}<br/>
                            A/C: ${settings.accountNumber} | IFSC: ${settings.ifscCode}<br/>
                            UPI ID: <strong>${settings.upiId}</strong>
                        </div>
                    </div>
                </div>

                <div>
                    <table class="summary-table">
                        <tr>
                            <td style="padding: 6px; color: #475569;">Subtotal:</td>
                            <td style="padding: 6px; text-align:right; font-weight:600;">${CurrencyUtils.formatInr(quote.subtotal)}</td>
                        </tr>
                        ${if (quote.offerDiscount > 0) """
                        <tr>
                            <td style="padding: 6px; color: #059669;">Special Offer Discount:</td>
                            <td style="padding: 6px; text-align:right; color: #059669; font-weight:600;">-${CurrencyUtils.formatInr(quote.offerDiscount)}</td>
                        </tr>
                        """.trimIndent() else ""}
                        ${if (quote.couponDiscount > 0) """
                        <tr>
                            <td style="padding: 6px; color: #059669;">Coupon (${quote.couponCode}):</td>
                            <td style="padding: 6px; text-align:right; color: #059669; font-weight:600;">-${CurrencyUtils.formatInr(quote.couponDiscount)}</td>
                        </tr>
                        """.trimIndent() else ""}
                        ${if (quote.manualDiscount > 0) """
                        <tr>
                            <td style="padding: 6px; color: #059669;">Negotiated Discount:</td>
                            <td style="padding: 6px; text-align:right; color: #059669; font-weight:600;">-${CurrencyUtils.formatInr(quote.manualDiscount)}</td>
                        </tr>
                        """.trimIndent() else ""}
                        <tr style="border-top: 1px solid #cbd5e1;">
                            <td style="padding: 6px; color: #1e293b; font-weight:bold;">Taxable Value:</td>
                            <td style="padding: 6px; text-align:right; font-weight:bold;">${CurrencyUtils.formatInr(quote.taxableAmount)}</td>
                        </tr>
                        $gstRows
                        <tr class="grand-total-row">
                            <td style="padding: 10px 6px; font-size: 15px; font-weight: bold; color: #1e3a8a;">Grand Total:</td>
                            <td style="padding: 10px 6px; text-align:right; font-size: 16px; font-weight: bold; color: #1e3a8a;">${CurrencyUtils.formatInr(quote.grandTotal)}</td>
                        </tr>
                    </table>
                </div>
            </div>

            <div class="footer">
                <strong>Terms & Conditions:</strong><br/>
                ${(quote.termsAndConditions.ifEmpty { settings.termsAndConditions }).replace("\n", "<br/>")}
                <br/><br/>
                <div style="display:flex; justify-content:space-between; margin-top:20px;">
                    <div>Authorized Signatory<br/><strong>${settings.businessName}</strong></div>
                    <div style="text-align:right;">Client Acceptance Signature<br/><strong>${quote.customerName}</strong></div>
                </div>
            </div>
        </body>
        </html>
        """.trimIndent()
    }

    fun generateInvoiceHtml(
        invoice: InvoiceEntity,
        items: List<InvoiceItemEntity>,
        settings: BusinessSettingsEntity
    ): String {
        val rows = items.mapIndexed { idx, item ->
            """
            <tr>
                <td style="text-align:center; padding: 8px; border: 1px solid #cbd5e1;">${idx + 1}</td>
                <td style="padding: 8px; border: 1px solid #cbd5e1;">
                    <strong>${item.itemName}</strong>
                    ${if (item.description.isNotEmpty()) "<br/><span style='font-size:11px; color:#64748b;'>${item.description}</span>" else ""}
                </td>
                <td style="text-align:center; padding: 8px; border: 1px solid #cbd5e1;">${item.hsnSacCode}</td>
                <td style="text-align:center; padding: 8px; border: 1px solid #cbd5e1;">${item.quantity}</td>
                <td style="text-align:right; padding: 8px; border: 1px solid #cbd5e1;">${CurrencyUtils.formatInr(item.rate)}</td>
                <td style="text-align:right; padding: 8px; border: 1px solid #cbd5e1;">${CurrencyUtils.formatInr(item.taxableAmount)}</td>
                <td style="text-align:right; padding: 8px; border: 1px solid #cbd5e1;"><strong>${CurrencyUtils.formatInr(item.total)}</strong></td>
            </tr>
            """.trimIndent()
        }.joinToString("\n")

        val gstRows = if (invoice.gstType == "CGST_SGST") {
            """
            <tr>
                <td style="padding: 6px; color: #475569;">CGST @ ${(invoice.gstRate / 2)}%:</td>
                <td style="padding: 6px; text-align:right; font-weight:600;">${CurrencyUtils.formatInr(invoice.cgstAmount)}</td>
            </tr>
            <tr>
                <td style="padding: 6px; color: #475569;">SGST @ ${(invoice.gstRate / 2)}%:</td>
                <td style="padding: 6px; text-align:right; font-weight:600;">${CurrencyUtils.formatInr(invoice.sgstAmount)}</td>
            </tr>
            """.trimIndent()
        } else {
            """
            <tr>
                <td style="padding: 6px; color: #475569;">IGST @ ${invoice.gstRate}%:</td>
                <td style="padding: 6px; text-align:right; font-weight:600;">${CurrencyUtils.formatInr(invoice.igstAmount)}</td>
            </tr>
            """.trimIndent()
        }

        return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="utf-8"/>
            <title>Tax Invoice - ${invoice.invoiceNumber}</title>
            <style>
                body { font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; color: #1e293b; padding: 24px; font-size: 13px; line-height: 1.5; }
                .header { display: flex; justify-content: space-between; border-bottom: 2px solid #059669; padding-bottom: 16px; margin-bottom: 20px; }
                .logo-title { font-size: 24px; font-weight: bold; color: #065f46; }
                .badge { background: #d1fae5; color: #065f46; padding: 4px 10px; border-radius: 4px; font-weight: bold; display: inline-block; font-size: 12px; }
                .info-grid { display: flex; justify-content: space-between; margin-bottom: 24px; }
                .info-box { width: 48%; }
                table { width: 100%; border-collapse: collapse; margin-bottom: 20px; }
                th { background-color: #f1f5f9; color: #1e293b; font-weight: 700; padding: 10px; border: 1px solid #cbd5e1; font-size: 12px; }
                .summary-table { width: 340px; margin-left: auto; border-collapse: collapse; }
                .grand-total-row { background-color: #ecfdf5; border-top: 2px solid #059669; border-bottom: 2px solid #059669; }
                .footer { margin-top: 30px; border-top: 1px solid #e2e8f0; padding-top: 16px; font-size: 11px; color: #64748b; }
            </style>
        </head>
        <body>
            <div class="header">
                <div>
                    <div class="logo-title">${settings.businessName}</div>
                    <div style="color: #475569; font-size: 12px;">${settings.tagline}</div>
                    <div style="margin-top: 6px; font-size: 12px;"><strong>GSTIN:</strong> ${settings.gstin} | <strong>PAN:</strong> ${settings.pan}</div>
                    <div style="font-size: 12px;">${settings.address}</div>
                </div>
                <div style="text-align: right;">
                    <div class="badge">TAX INVOICE (ORIGINAL FOR RECIPIENT)</div>
                    <h2 style="margin: 6px 0 0 0; color: #0f172a;">${invoice.invoiceNumber}</h2>
                    <div style="color: #64748b; font-size: 12px;">Invoice Date: ${CurrencyUtils.formatDate(invoice.issueDate)}</div>
                    <div style="color: #64748b; font-size: 12px;">Due Date: ${CurrencyUtils.formatDate(invoice.dueDate)}</div>
                    <div style="margin-top: 4px;"><span class="badge" style="background:#e0e7ff; color:#3730a3;">Payment: ${invoice.status}</span></div>
                </div>
            </div>

            <div class="info-grid">
                <div class="info-box">
                    <strong style="color: #475569; text-transform: uppercase; font-size: 11px;">Billed To:</strong>
                    <div style="font-size: 15px; font-weight: bold; color: #0f172a; margin-top: 4px;">${invoice.customerName}</div>
                    ${if (invoice.customerBusiness.isNotEmpty()) "<div style='font-weight:600;'>${invoice.customerBusiness}</div>" else ""}
                    ${if (invoice.customerAddress.isNotEmpty()) "<div>Address: ${invoice.customerAddress}</div>" else ""}
                    ${if (invoice.customerGstin.isNotEmpty()) "<div><strong>GSTIN:</strong> ${invoice.customerGstin}</div>" else ""}
                </div>
                <div class="info-box" style="text-align: right;">
                    <strong style="color: #475569; text-transform: uppercase; font-size: 11px;">Project:</strong>
                    <div style="font-size: 15px; font-weight: bold; color: #059669; margin-top: 4px;">${invoice.projectName}</div>
                    <div style="color: #475569;">State of Supply: Maharashtra (27)</div>
                </div>
            </div>

            <table>
                <thead>
                    <tr>
                        <th style="width: 5%;">#</th>
                        <th style="width: 40%; text-align: left;">Item Description</th>
                        <th style="width: 12%;">HSN/SAC</th>
                        <th style="width: 8%;">Qty</th>
                        <th style="width: 15%; text-align: right;">Rate</th>
                        <th style="width: 15%; text-align: right;">Taxable Value</th>
                        <th style="width: 15%; text-align: right;">Total (+GST)</th>
                    </tr>
                </thead>
                <tbody>
                    $rows
                </tbody>
            </table>

            <div style="display: flex; justify-content: space-between; align-items: flex-start;">
                <div style="width: 50%;">
                    <div style="background:#f8fafc; padding:10px; border:1px solid #e2e8f0; border-radius:6px;">
                        <strong>Amount in Words:</strong><br/>
                        <span style="font-style: italic; color:#1e293b;">${CurrencyUtils.amountToWords(invoice.grandTotal)}</span>
                    </div>

                    <div style="margin-top: 14px;">
                        <strong>Bank Transfer & UPI QR:</strong>
                        <div style="font-size: 12px; color:#475569; margin-top:4px;">
                            Bank Name: ${settings.bankName}<br/>
                            Account No: ${settings.accountNumber} | IFSC: ${settings.ifscCode}<br/>
                            Account Holder: ${settings.accountHolder}<br/>
                            UPI VPA: <strong>${settings.upiId}</strong>
                        </div>
                    </div>
                </div>

                <div>
                    <table class="summary-table">
                        <tr>
                            <td style="padding: 6px; color: #475569;">Taxable Amount:</td>
                            <td style="padding: 6px; text-align:right; font-weight:600;">${CurrencyUtils.formatInr(invoice.taxableTotal)}</td>
                        </tr>
                        $gstRows
                        <tr class="grand-total-row">
                            <td style="padding: 8px 6px; font-size: 15px; font-weight: bold; color: #065f46;">Invoice Total:</td>
                            <td style="padding: 8px 6px; text-align:right; font-size: 16px; font-weight: bold; color: #065f46;">${CurrencyUtils.formatInr(invoice.grandTotal)}</td>
                        </tr>
                        <tr>
                            <td style="padding: 6px; color: #059669; font-weight:600;">Paid Amount:</td>
                            <td style="padding: 6px; text-align:right; color: #059669; font-weight:bold;">${CurrencyUtils.formatInr(invoice.paidAmount)}</td>
                        </tr>
                        <tr style="border-top: 1px solid #cbd5e1;">
                            <td style="padding: 6px; color: #dc2626; font-weight:bold;">Balance Due:</td>
                            <td style="padding: 6px; text-align:right; color: #dc2626; font-weight:bold; font-size:15px;">${CurrencyUtils.formatInr(invoice.dueAmount)}</td>
                        </tr>
                    </table>
                </div>
            </div>

            <div class="footer">
                <strong>Terms & Conditions:</strong><br/>
                1. Please pay balance before final source code handover.<br/>
                2. Subject to Pune jurisdiction.<br/><br/>
                <div style="text-align:right; margin-top:20px;">
                    For <strong>${settings.businessName}</strong><br/><br/><br/>
                    Authorized Signatory
                </div>
            </div>
        </body>
        </html>
        """.trimIndent()
    }

    /**
     * Triggers the Android Print framework to generate PDF or print to hardware printer
     */
    fun printHtmlDocument(context: Context, htmlContent: String, jobName: String) {
        val webView = WebView(context)
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
                val printAdapter = webView.createPrintDocumentAdapter(jobName)
                val printAttributes = PrintAttributes.Builder()
                    .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                    .setResolution(PrintAttributes.Resolution("smartquote_res", "SmartQuote", 300, 300))
                    .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                    .build()

                printManager?.print(jobName, printAdapter, printAttributes)
            }
        }
        webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
    }

    /**
     * Shares a beautifully formatted quotation breakdown directly to WhatsApp
     */
    fun shareQuotationToWhatsApp(context: Context, quote: QuotationEntity, items: List<QuotationItemEntity>, settings: BusinessSettingsEntity) {
        val itemBullets = items.joinToString("\n") { "• ${it.serviceName}: ${CurrencyUtils.formatInr(it.taxableAmount)}" }
        val message = """
        *QUOTATION: ${quote.quotationNumber}*
        🏢 *${settings.businessName}* (GSTIN: ${settings.gstin})
        
        👤 *Client:* ${quote.customerName}
        🚀 *Project:* ${quote.projectName}
        
        📋 *Requirement Breakdown:*
        $itemBullets
        
        ───────────────
        💰 *Subtotal:* ${CurrencyUtils.formatInr(quote.subtotal)}
        ${if (quote.totalSavings > 0) "🎁 *Total Discount/Savings:* -${CurrencyUtils.formatInr(quote.totalSavings)}\n" else ""}📊 *Taxable Amount:* ${CurrencyUtils.formatInr(quote.taxableAmount)}
        🏛️ *GST (${quote.gstRate}%):* ${CurrencyUtils.formatInr(quote.cgstAmount + quote.sgstAmount + quote.igstAmount)}
        ⭐ *Grand Total:* *${CurrencyUtils.formatInr(quote.grandTotal)}*
        ───────────────
        
        📅 *Valid Until:* ${CurrencyUtils.formatDate(quote.validUntil)}
        
        💬 Please reply to confirm or request adjustments!
        """.trimIndent()

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, message)
            setPackage("com.whatsapp")
        }

        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            // If whatsapp app is not installed, open generic share chooser
            val chooser = Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, message)
            }, "Share Quotation Summary")
            context.startActivity(chooser)
        }
    }

    fun shareViaEmail(context: Context, quote: QuotationEntity, items: List<QuotationItemEntity>, settings: BusinessSettingsEntity) {
        val subject = "Quotation [${quote.quotationNumber}] - ${quote.projectName} | ${settings.businessName}"
        val body = """
        Dear ${quote.customerName},

        Thank you for your interest in partnering with ${settings.businessName}.
        We are pleased to present the official quotation for "${quote.projectName}".

        Quotation Details:
        - Quotation Number: ${quote.quotationNumber}
        - Total Services: ${items.size} modules
        - Taxable Amount: ${CurrencyUtils.formatInr(quote.taxableAmount)}
        - Grand Total (inc. GST): ${CurrencyUtils.formatInr(quote.grandTotal)}
        - Validity: ${CurrencyUtils.formatDate(quote.validUntil)}

        Payment Terms:
        30% Advance booking, milestone-based releases.

        Feel free to contact us at ${settings.phone} or ${settings.email}.

        Warm regards,
        ${settings.businessName} Team
        GSTIN: ${settings.gstin}
        """.trimIndent()

        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf(quote.customerEmail))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
        }
        try {
            context.startActivity(Intent.createChooser(intent, "Send Quotation via Email"))
        } catch (e: Exception) {
            Toast.makeText(context, "No email client found", Toast.LENGTH_SHORT).show()
        }
    }
}
