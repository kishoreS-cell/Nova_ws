package com.leaseapp.util;

import com.leaseapp.model.Lease;
import com.leaseapp.model.homeowner.*;
import com.leaseapp.model.tenant.AttachmentAcknowledgment;
import com.leaseapp.model.tenant.Occupant;
import com.leaseapp.model.tenant.Tenant;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;

/**
 * Fills the static Nova Scotia Form P lease template by drawing text at exact
 * coordinates measured from the template PDF itself (it has no real AcroForm
 * fields, so PDFBox's PDAcroForm approach cannot be used here).
 *
 * Coordinate system note: pdfplumber (used to measure these positions) reports
 * "top" as distance from the TOP of the page. PDFBox draws using a bottom-left
 * origin, so every y value below is computed as (pageHeight - top).
 */
public class LeasePdfGenerator {

    private static final PDType1Font FONT = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
    // ZapfDingbats is one of the 14 standard PDF fonts, so no embedding is needed.
    // PDFBox's showText() maps each Unicode character to a glyph name via the font's
    // AGL, then to that font's internal code - so this must be the actual Unicode
    // check-mark character (U+2714), not the digit '4' from the encoding table.
    private static final PDType1Font CHECK_FONT = new PDType1Font(Standard14Fonts.FontName.ZAPF_DINGBATS);
    private static final String CHECK_GLYPH = "\u2714";
    private static final float FONT_SIZE = 10f;
    private static final float PAGE_HEIGHT = 792f; // US Letter, matches the template

    public byte[] generate(Lease lease, List<Tenant> tenants, List<Occupant> occupants) throws IOException {

        try (InputStream templateStream = getClass().getResourceAsStream("/lease_template.pdf")) {

            if (templateStream == null) {
                throw new IOException("lease_template.pdf not found on classpath - " +
                        "make sure it's placed in src/main/resources");
            }

            byte[] templateBytes = templateStream.readAllBytes();

            try (PDDocument document = Loader.loadPDF(templateBytes)) {

            LandlordInfo landlord = lease.getLandlordInfo();
            PremisesInfo premises = lease.getPremisesInfo();
            LandlordServiceEmail serviceEmail = lease.getLandlordServiceEmail();
            LeaseTerm leaseTerm = lease.getLeaseTerm();
            RentDetails rent = lease.getRentDetails();
            RentIncludes includes = lease.getRentIncludes();
            TenantResponsibilities resp = lease.getTenantResponsibilities();
            SecurityDeposit deposit = lease.getSecurityDeposit();

            // ---------------- PAGE 1 (index 0) - Sections 1, 2, 3 ----------------
            writeText(document, 0, 81, 181.4f, landlord.getName());
            writeText(document, 0, 179.2f, 214.6f, landlord.getStreetAddress());
            writeText(document, 0, 156.6f, 234.8f, landlord.getMailingAddress());
            writeText(document, 0, 132.9f, 255.0f, landlord.getApartment());
            writeText(document, 0, 265.9f, 255.0f, landlord.getCity());
            writeText(document, 0, 505.3f, 255.0f, landlord.getPostalCode());
            writeText(document, 0, 148.6f, 275.2f, landlord.getPhoneHome());
            writeText(document, 0, 406.0f, 275.2f, landlord.getPhoneBusiness());

            // Tenant name lines (up to 3) - pulled from the tenants already added to this lease
            float[] tenantLineTops = {313.5f, 333.7f, 353.9f};
            for (int i = 0; i < tenants.size() && i < 3; i++) {
                writeText(document, 0, 249.9f, tenantLineTops[i], tenants.get(i).getFullName());
            }

            // Occupants (Section 2) - up to 4 lines
            float[] occupantLineTops = {426.0f, 446.2f, 466.4f, 486.6f};
            for (int i = 0; i < occupants.size() && i < 4; i++) {
                Occupant o = occupants.get(i);
                writeText(document, 0, 81, occupantLineTops[i], o.getFullName() + " (age " + o.getAge() + ")");
            }

            // Section 4 - Emergency contact (tenant-filled, stored at lease level)
            com.leaseapp.model.tenant.EmergencyContact ec = lease.getEmergencyContact();
            writeText(document, 1, 173, 151.8f, ec.getNextOfKin());
            writeText(document, 1, 158, 175.6f, ec.getPhoneWork());
            writeText(document, 1, 379, 175.6f, ec.getPhoneHome());

            // Section 8 - Email address(es) for service by landlord on tenant(s) - up to 3 lines
            float[] serviceEmailTops = {698.4f, 722.2f, 746.0f};
            for (int i = 0; i < tenants.size() && i < 3; i++) {
                writeText(document, 1, 153, serviceEmailTops[i], tenants.get(i).getEmailForService());
            }

            // Section 3 - Premises
            writeText(document, 0, 179.2f, 621.5f, premises.getStreetAddress());
            writeText(document, 0, 132.9f, 641.7f, premises.getApartment());
            writeText(document, 0, 265.9f, 641.7f, premises.getCity());
            writeText(document, 0, 505.3f, 641.7f, premises.getPostalCode());
            writeText(document, 0, 81, 684.3f, premises.getPropertyType());
            writeText(document, 0, 186.4f, 719.6f, premises.getTenantMailingPobox());
            writeText(document, 0, 504.5f, 719.6f, premises.getTenantMailingPostalCode());
            writeText(document, 0, 189.2f, 739.8f, premises.getTenantPhoneHome());
            writeText(document, 0, 470.4f, 739.8f, premises.getTenantPhoneBusiness());

            // ---------------- PAGE 3 (index 2) - Section 9, 11A ----------------
            writeText(document, 2, 148.8f, 334.8f, serviceEmail.getEmailAddress());

            if ("PERIODIC".equals(leaseTerm.getLeaseType()) && leaseTerm.getPeriodicStartDate() != null) {
                java.time.LocalDate d = leaseTerm.getPeriodicStartDate().toLocalDate();
                writeText(document, 2, 265.8f, 619.8f, String.valueOf(d.getDayOfMonth()));
                writeText(document, 2, 433.0f, 619.8f, d.getMonth().toString());
                writeText(document, 2, 542.8f, 619.8f, String.valueOf(d.getYear()).substring(2));
                checkBox(document, 2, 99, periodicTermTop(leaseTerm.getPeriodicTerm()));
            }

            // ---------------- PAGE 4 (index 3) - Section 11B, 13 ----------------
            if ("FIXED_TERM".equals(leaseTerm.getLeaseType())) {
                if (leaseTerm.getFixedStartDate() != null) {
                    java.time.LocalDate d = leaseTerm.getFixedStartDate().toLocalDate();
                    writeText(document, 3, 146.3f, 165.0f, String.valueOf(d.getDayOfMonth()));
                    writeText(document, 3, 427.5f, 165.0f, d.getMonth().toString());
                    writeText(document, 3, 537.3f, 165.0f, String.valueOf(d.getYear()).substring(2));
                }
                if (leaseTerm.getFixedEndDate() != null) {
                    java.time.LocalDate d = leaseTerm.getFixedEndDate().toLocalDate();
                    writeText(document, 3, 201.3f, 193.3f, String.valueOf(d.getDayOfMonth()));
                    writeText(document, 3, 424.9f, 193.3f, d.getMonth().toString());
                    writeText(document, 3, 534.7f, 193.3f, String.valueOf(d.getYear()).substring(2));
                }
            }

            if (rent.getAmount() != null) {
                writeText(document, 3, 212.1f, 413.0f, rent.getAmount().toPlainString());
            }
            checkBox(document, 3, 99, "WEEK".equals(rent.getFrequency()) ? 421.8f : 434.8f);
            checkBox(document, 3, 99, paymentMethodTop(rent.getPaymentMethod()));
            if ("OTHER".equals(rent.getPaymentMethod())) {
                writeText(document, 3, 210, 513.0f, rent.getPaymentMethodOther());
            }
            if (rent.getDueDay() > 0) {
                writeText(document, 3, 175, 541.3f, String.valueOf(rent.getDueDay()));
            }
            writeText(document, 3, 150, 562.4f, rent.getPayableTo());

            // ---------------- PAGE 5 (index 4) - Section 16 ----------------
            checkIf(document, 4, 81, 460.9f, includes.isStove());
            checkIf(document, 4, 81, 473.9f, includes.isFridge());
            checkIf(document, 4, 81, 486.9f, includes.isWasherDryer());
            checkIf(document, 4, 81, 499.9f, includes.isDishwasher());
            checkIf(document, 4, 81, 512.9f, includes.isFurniture());

            checkIf(document, 4, 178, 460.9f, includes.isCoinLaundry());
            checkIf(document, 4, 178, 473.9f, includes.isCable());
            checkIf(document, 4, 178, 486.9f, includes.isHeat());
            checkIf(document, 4, 178, 499.9f, includes.isWater());
            checkIf(document, 4, 178, 512.9f, includes.isHotWater());
            checkIf(document, 4, 178, 525.9f, includes.isElectricity());
            checkIf(document, 4, 178, 551.9f, includes.isRecyclingFacilities());

            checkIf(document, 4, 432, 460.9f, includes.isLawnCare());
            checkIf(document, 4, 432, 473.9f, includes.isSnowRemoval());
            checkIf(document, 4, 432, 486.9f, includes.isGarbageRemoval());

            if (includes.getParkingSpaces() > 0) {
                writeText(document, 4, 286.9f, 547.7f, String.valueOf(includes.getParkingSpaces()));
                checkBox(document, 4, 178, 538.9f);
            }
            if (includes.getParkingSpaceNumber() != null && !includes.getParkingSpaceNumber().isBlank()) {
                writeText(document, 4, 362.4f, 547.7f, includes.getParkingSpaceNumber());
            }
            if (includes.getOtherCustom() != null && !includes.getOtherCustom().isBlank()) {
                writeText(document, 4, 445.3f, 508.7f, includes.getOtherCustom());
            }

            // Tenant responsibilities checkboxes + limits
            checkIf(document, 4, 99, 616.5f, resp.isLawnCare());
            checkIf(document, 4, 99, 629.5f, resp.isLatePayment());
            checkIf(document, 4, 99, 642.5f, resp.isSnowRemoval());
            checkIf(document, 4, 99, 668.5f, resp.isGarbageRemoval());
            checkIf(document, 4, 99, 694.5f, resp.isTenantInsurance());
            checkIf(document, 4, 99, 720.5f, resp.isSeparationRecyclables());

            if (resp.getReturnedChequeLimit() != null) {
                writeText(document, 4, 294.3f, 664.3f, "$" + resp.getReturnedChequeLimit().toPlainString());
                checkBox(document, 4, 99, 655.5f);
            }
            if (resp.getParkingFee() != null) {
                writeText(document, 4, 163.9f, 690.3f, "$" + resp.getParkingFee().toPlainString());
                writeText(document, 4, 327.2f, 690.3f, String.valueOf(resp.getParkingSpaces()));
                checkBox(document, 4, 99, 681.5f);
            }
            if (resp.getLockoutChargeLimit() != null) {
                writeText(document, 4, 291.8f, 716.3f, "$" + resp.getLockoutChargeLimit().toPlainString());
                checkBox(document, 4, 99, 707.5f);
            }

            // ---------------- PAGE 6 (index 5) - Section 17, 18, 19 ----------------
            if (lease.getAdditionalObligations() != null && !lease.getAdditionalObligations().isBlank()) {
                writeWrappedText(document, 5, 81, 151.8f, lease.getAdditionalObligations(), 480f);
            }

            if (!deposit.isRequired()) {
                checkBox(document, 5, 81, 233.6f);
            } else {
                checkBox(document, 5, 81, 268.6f);
                if (deposit.getAmount() != null) {
                    writeText(document, 5, 198.9f, 277.3f, "$" + deposit.getAmount().toPlainString());
                }
                if (deposit.getInstitution() != null) {
                    writeText(document, 5, 99, 307.8f, deposit.getInstitution());
                }
            }

            if (lease.isInspectionReportAttached()) {
                checkBox(document, 5, 99, 443.8f);
            } else {
                checkBox(document, 5, 99, 460.4f);
            }

            // ---------------- PAGE 6 (index 5) - Section 20 ----------------
            if (lease.isRulesAttached()) {
                checkBox(document, 5, 81, 530.7f);
                if (lease.getRulesScheduleRef() != null && !lease.getRulesScheduleRef().isBlank()) {
                    writeText(document, 5, 482, 532.1f, lease.getRulesScheduleRef());
                }
            }

            // ---------------- PAGE 8 (index 7) - Section 26 attachments acknowledgment ----------------
            // Only checked once ANY tenant confirms - the template has one shared checkbox per item,
            // not one per tenant, so a single confirmation covers the application.
            for (Tenant t : tenants) {
                AttachmentAcknowledgment ack = t.getAttachmentAcknowledgment();
                if (ack.isReceivedAct()) {
                    checkBox(document, 7, 154, 199.6f); // "paper copy" - the option offered on the tenant form
                }
                break; // one mark is enough regardless of how many tenants confirmed
            }

            // ---------------- PAGE 9 (index 8) - Signatures ----------------
            if (lease.getHomeownerSignatureData() != null) {
                if (lease.getHomeownerSignedDate() != null) {
                    writeText(document, 8, 182, 431.8f, formatSignedDate(lease.getHomeownerSignedDate().toLocalDate()));
                }
                drawSignatureImage(document, 8, lease.getHomeownerSignatureData(), 374.3f, 431.8f, 150f);
            }

            float[] tenantSigTops = {506.8f, 534.2f, 561.6f};
            for (int i = 0; i < tenants.size() && i < 3; i++) {
                Tenant t = tenants.get(i);
                if (t.getSignatureData() == null) continue;
                if (t.getSignedDate() != null) {
                    writeText(document, 8, 182, tenantSigTops[i], formatSignedDate(t.getSignedDate().toLocalDate()));
                }
                drawSignatureImage(document, 8, t.getSignatureData(), 365.8f, tenantSigTops[i], 150f);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            document.save(out);
            return out.toByteArray();
            }
        }
    }

    // ---------------- Helper methods ----------------

    private void writeText(PDDocument document, int pageIndex, float x, float top, String value) throws IOException {
        if (value == null || value.isBlank()) return;
        PDPage page = document.getPage(pageIndex);
        float y = PAGE_HEIGHT - top + 2; // +2 nudges text to sit just above the underline
        try (PDPageContentStream cs = new PDPageContentStream(document, page,
                PDPageContentStream.AppendMode.APPEND, true, true)) {
            cs.beginText();
            cs.setFont(FONT, FONT_SIZE);
            cs.newLineAtOffset(x, y);
            cs.showText(value);
            cs.endText();
        }
    }

    // Wraps long text (e.g. Additional Obligations) across multiple lines within maxWidth
    private void writeWrappedText(PDDocument document, int pageIndex, float x, float startTop, String text, float maxWidth) throws IOException {
        String[] words = text.split("\\s+");
        StringBuilder line = new StringBuilder();
        float lineSpacing = 20.2f; // matches the template's blank-line spacing on this page
        float top = startTop;

        for (String word : words) {
            String candidate = line.length() == 0 ? word : line + " " + word;
            float width = FONT_SIZE * FONT.getStringWidth(candidate) / 1000f;
            if (width > maxWidth && line.length() > 0) {
                writeText(document, pageIndex, x, top, line.toString());
                line = new StringBuilder(word);
                top += lineSpacing;
            } else {
                line = new StringBuilder(candidate);
            }
        }
        if (line.length() > 0) {
            writeText(document, pageIndex, x, top, line.toString());
        }
    }

    // Draws a check mark on top of an existing checkbox glyph (the "q" character in the template font)
    private void checkBox(PDDocument document, int pageIndex, float x, float top) throws IOException {
        PDPage page = document.getPage(pageIndex);
        float y = PAGE_HEIGHT - top;
        try (PDPageContentStream cs = new PDPageContentStream(document, page,
                PDPageContentStream.AppendMode.APPEND, true, true)) {
            cs.beginText();
            cs.setFont(CHECK_FONT, FONT_SIZE);
            cs.newLineAtOffset(x + 0.5f, y - 8.5f);
            cs.showText(CHECK_GLYPH);
            cs.endText();
        }
    }

    private void checkIf(PDDocument document, int pageIndex, float x, float top, boolean condition) throws IOException {
        if (condition) {
            checkBox(document, pageIndex, x, top);
        }
    }

    private float periodicTermTop(String term) {
        if ("YEAR_TO_YEAR".equals(term)) return 655.8f;
        if ("MONTH_TO_MONTH".equals(term)) return 671.0f;
        return 686.1f; // WEEK_TO_WEEK
    }

    // Draws a signature captured as a "data:image/png;base64,...." string (from the <canvas> signature
    // pads on tenant_form.jsp / view_lease.jsp) just above the given underline, preserving the
    // canvas's 4:1 aspect ratio and capping the width so it never overruns the signature line.
    private void drawSignatureImage(PDDocument document, int pageIndex, String dataUrl, float x, float lineTop, float maxWidth) throws IOException {
        if (dataUrl == null || dataUrl.isBlank()) return;
        String base64 = dataUrl.contains(",") ? dataUrl.substring(dataUrl.indexOf(',') + 1) : dataUrl;
        byte[] imageBytes;
        try {
            imageBytes = Base64.getDecoder().decode(base64);
        } catch (IllegalArgumentException e) {
            return; // malformed signature data - skip rather than break the whole PDF
        }

        PDImageXObject image = PDImageXObject.createFromByteArray(document, imageBytes, "signature");
        float width = maxWidth;
        float height = width / 4f; // matches the 600x150 signature canvas aspect ratio

        PDPage page = document.getPage(pageIndex);
        float y = PAGE_HEIGHT - lineTop + 3; // sit just above the underline
        try (PDPageContentStream cs = new PDPageContentStream(document, page,
                PDPageContentStream.AppendMode.APPEND, true, true)) {
            cs.drawImage(image, x, y, width, height);
        }
    }

    private String formatSignedDate(java.time.LocalDate date) {
        return date.format(DateTimeFormatter.ofPattern("yyyy MM dd"));
    }

    private float paymentMethodTop(String method) {
        if (method == null) return 452.3f;
        switch (method) {
            case "CASH": return 452.3f;
            case "AUTO_WITHDRAWAL": return 465.3f;
            case "POST_DATED_CHEQUES": return 478.3f;
            case "CHEQUE": return 491.3f;
            default: return 504.3f; // OTHER
        }
    }
}