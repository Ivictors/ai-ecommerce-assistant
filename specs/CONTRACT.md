# Domain Contract

## 1. Purpose

This document defines currently approved business contracts and invariants for
the AI E-Commerce Assistant.

It describes behavior, not database structure.

Do not interpret every concept listed here as requiring its own database table
or Java class.

---

# Customer

A customer must be authenticated and registered to complete a purchase.

Unauthenticated users may:

- browse products;
- interact with a guest cart;
- simulate purchase values;
- simulate freight where supported.

They may not complete checkout/payment.

Customer information includes:

- name;
- CPF;
- email;
- phone;
- address.

CPF must be unique.

Email must be unique.

A customer can access only their own private customer/order information.

A customer currently has one active address.

Orders preserve the address used during purchase independently from future
changes to the customer's current address.

---

# Product

A Product represents the general catalog product.

It may contain:

- name;
- description;
- brand;
- categories;
- images;
- active/inactive state;
- variants.

A product may belong to multiple categories.

Deactivation must not destroy historical order information.

---

# ProductVariant

ProductVariant represents a purchasable variation of a Product.

A variant owns its:

- SKU;
- base price;
- stock association.

SKU must be unique.

Different variants may have different:

- SKU;
- price;
- stock.

The base price belongs to ProductVariant.

Promotional pricing must not overwrite this base price.

---

# Stock

Stock is responsible for inventory availability.

The system must never successfully reserve more units than are available.

Concurrent operations must preserve this invariant.

Products/variants without available stock cannot be sold.

Reservation is distinct from definitive stock consumption.

The exact concurrency implementation is an infrastructure decision, but it
must preserve the domain invariant.

---

# Cart

A customer has at most one active customer cart.

Guest carts are supported.

Adding an item to a cart does not reserve stock.

A cart does not freeze the product price.

The effective purchase price is determined during purchase/order creation.

If an item becomes unavailable while present in the cart, the system must not
allow it to be purchased as if stock were available.

A requested quantity cannot exceed allowed availability at the point where the
relevant business rule is validated.

---

# Order

Checkout creates an Order in:

`PENDING_PAYMENT`

The successful payment flow is conceptually:

Checkout
↓
Order = PENDING_PAYMENT
↓
Payment
↓
Payment = APPROVED
↓
Order = PAID

Current order lifecycle:

`PENDING_PAYMENT`

`PAID`

`PREPARATION`

`INVOICED`

`SHIPPED`

`DELIVERED`

`CANCELLED`

Expected primary flow:

PENDING_PAYMENT
↓
PAID
↓
PREPARATION
↓
INVOICED
↓
SHIPPED
↓
DELIVERED

Cancellation is permitted according to approved business rules before
shipment.

A shipped order is not cancelled through the normal cancellation flow.
Post-shipment reversal is handled through the return process when applicable.

Order owns its lifecycle.

Payment does not arbitrarily mutate Order state.

Generic unrestricted order status mutation must not be exposed.

---

# Order Historical Snapshot

An Order must preserve the commercial facts agreed at purchase time.

Historical information must not change because current catalog information
changes.

Order/order-item history must preserve the required purchase-time information,
including as applicable:

- product/variant identification;
- purchased quantity;
- unit price;
- applied discount;
- effective purchase price;
- address used for the order.

Changing ProductVariant.price after purchase must not change existing orders.

Changing the customer's current address must not change an existing order.

---

# Reservation

Checkout requires stock reservation according to the approved purchase flow.

A reservation lasts 30 minutes.

Conceptual flow:

Order = PENDING_PAYMENT
+
Reservation = ACTIVE

If payment is not successfully completed within the reservation period:

Reservation → EXPIRED

and reserved stock becomes available again according to the inventory model.

The associated unpaid order must follow the approved cancellation/expiration
behavior.

Reservation lifecycle and Order lifecycle are related but are not the same
concept.

---

# Payment

Payment owns payment state.

Current payment states:

`PENDING`

`APPROVED`

`DECLINED`

`REFUNDED`

Supported payment methods:

- Pix;
- card.

Payment processing is performed through an external gateway.

Gateway results are received asynchronously through webhook integration.

Webhook processing must be idempotent.

Repeated webhook delivery must not duplicate business effects.

Payment approval allows the corresponding order transition according to the
Order rules.

The frontend and LLM cannot declare a payment approved.

---

# Promotion

ProductVariant owns its base price.

Promotion represents an optional commercial rule affecting the effective
price.

Promotion must not overwrite ProductVariant.basePrice.

Conceptually:

Base Price
↓
Applicable Promotion(s)
↓
Effective Price
↓
Purchase Snapshot

Promotions may contain:

- validity period;
- percentage;
- combinability rule.

Promotions may apply to ProductVariant according to currently approved
requirements.

The administrator configures promotions.

Historical order discounts remain unchanged after purchase.

The exact algorithm for combining multiple promotions remains a domain
decision that must be explicitly specified before implementation if not
already defined elsewhere.

---

# Return

Return represents the return/refund workflow.

Current lifecycle:

`REQUESTED`

`UNDER_REVIEW`

`APPROVED`

`REJECTED`

`REFUNDED`

`RETURN_RECEIVED` is a recommended intermediate state and must not be treated
as approved until explicitly incorporated into the domain specification.

The administrator evaluates return requests.

Relevant evaluation information includes:

- deadline;
- product condition;
- label condition;
- evidence of use;
- violation/damage;
- whether the returned item is correct;
- defect information where applicable.

Return eligibility currently uses a seven-day period after receipt according
to the approved requirement.

Refund value is based on the amount actually paid, not the product's current
catalog price.

Returning an item must not automatically make it available as sellable stock
before the appropriate physical inspection/business decision.

---

# Delivery

Supported fulfillment options include:

- Correios delivery;
- free store pickup.

Correios freight is added to the order total and paid by the customer.

Freight calculation must use trusted backend integration.

The frontend or LLM must not authoritatively determine freight price.

Orders may contain tracking information when delivery is applicable.

---

# Notifications

Email is the required notification channel.

Relevant events include:

- order created;
- payment approved;
- shipped;
- delivered;
- cancelled.

Notification delivery must be separated from the success of the primary
business transaction where appropriate.

A notification failure must not automatically invalidate an otherwise
successful purchase operation.

---

# Administrator

The current system has one administrator role/profile for business
administration.

Administrative capabilities include, according to the relevant use case:

- catalog management;
- variant/price management;
- stock management;
- promotion management;
- return evaluation;
- customer administration;
- operational reporting.

Administrative authorization must be enforced by the backend.

---

# AI Assistant

The AI assistant is an interaction/orchestration layer.

It does not own e-commerce truth.

The assistant may use Tools to invoke approved application capabilities.

It must retrieve authoritative data for questions involving:

- price;
- stock;
- products;
- orders;
- customer information;
- promotions;
- business state.

The model must not fabricate authoritative business values when the required
information is unavailable.

Tools must use application services rather than bypassing business rules.