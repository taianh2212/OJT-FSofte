package com.tourbooking.booking.service;

import com.tourbooking.booking.model.entity.Invoice;

public interface InvoiceService {

    Invoice getInvoice(Long bookingId);

    Invoice generateInvoice(Long bookingId);
}
