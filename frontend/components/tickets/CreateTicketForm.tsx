"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useId, useRef, useState, type FormEvent } from "react";
import { InlineAlert } from "@/components/ui/InlineAlert";
import { ApiClientError } from "@/lib/api/http";
import { createTicket } from "@/lib/api/tickets";
import { PRIORITIES, type ErrorDetail, type Priority } from "@/lib/types/api";

type FieldErrors = Partial<Record<"title" | "description" | "priority" | "assignee", string>>;

const TITLE_MAX = 200;
const DESCRIPTION_MAX = 10_000;
const ASSIGNEE_MAX = 120;

/**
 * Create-ticket form (UIF-002 / DEC-009b/f). Status is never collected.
 */
export function CreateTicketForm() {
  const router = useRouter();
  const formId = useId();
  const titleRef = useRef<HTMLInputElement>(null);

  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [priority, setPriority] = useState<Priority | "">("");
  const [assignee, setAssignee] = useState("");

  const [fieldErrors, setFieldErrors] = useState<FieldErrors>({});
  const [formError, setFormError] = useState<{ message: string; details?: ErrorDetail[] } | null>(
    null
  );
  const [submitting, setSubmitting] = useState(false);

  function validateClient(): FieldErrors {
    const errors: FieldErrors = {};
    const trimmedTitle = title.trim();

    if (!trimmedTitle) {
      errors.title = "Title is required";
    } else if (trimmedTitle.length > TITLE_MAX) {
      errors.title = `Title must be at most ${TITLE_MAX} characters`;
    }

    if (description.length > DESCRIPTION_MAX) {
      errors.description = `Description must be at most ${DESCRIPTION_MAX} characters`;
    }

    if (assignee.trim().length > ASSIGNEE_MAX) {
      errors.assignee = `Assignee must be at most ${ASSIGNEE_MAX} characters`;
    }

    if (priority !== "" && !PRIORITIES.includes(priority)) {
      errors.priority = "Priority must be LOW, MEDIUM, or HIGH";
    }

    return errors;
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (submitting) {
      return;
    }

    const clientErrors = validateClient();
    setFieldErrors(clientErrors);
    setFormError(null);

    if (Object.keys(clientErrors).length > 0) {
      titleRef.current?.focus();
      return;
    }

    setSubmitting(true);
    try {
      const created = await createTicket({
        title,
        description: description.trim() ? description : undefined,
        priority: priority || undefined,
        assignee: assignee.trim() ? assignee : undefined,
      });
      router.push(`/tickets/${created.id}`);
    } catch (err) {
      if (err instanceof ApiClientError) {
        const details = err.errorDto?.details ?? [];
        const nextFieldErrors: FieldErrors = {};
        for (const detail of details) {
          const field = detail.field;
          if (
            field === "title" ||
            field === "description" ||
            field === "priority" ||
            field === "assignee"
          ) {
            nextFieldErrors[field] = detail.message;
          }
        }
        setFieldErrors(nextFieldErrors);
        setFormError({
          message: err.message,
          details: details.filter(
            (detail) =>
              detail.field !== "title" &&
              detail.field !== "description" &&
              detail.field !== "priority" &&
              detail.field !== "assignee"
          ),
        });
      } else {
        setFormError({ message: "Unable to create ticket. Please try again." });
      }
      setSubmitting(false);
    }
  }

  return (
    <section className="create-ticket">
      <div className="create-ticket__header">
        <h1>Create ticket</h1>
        <p className="create-ticket__subtitle">
          Status starts as OPEN automatically. You do not choose an initial status.
        </p>
      </div>

      {formError ? (
        <InlineAlert
          tone="error"
          title="Could not create ticket"
          message={formError.message}
          details={formError.details}
        />
      ) : null}

      <form className="ticket-form" onSubmit={handleSubmit} noValidate aria-busy={submitting}>
        <div className="field">
          <label className="field__label" htmlFor={`${formId}-title`}>
            Title <span className="field__required">(required)</span>
          </label>
          <input
            ref={titleRef}
            id={`${formId}-title`}
            name="title"
            type="text"
            value={title}
            maxLength={TITLE_MAX}
            onChange={(event) => setTitle(event.target.value)}
            aria-invalid={Boolean(fieldErrors.title)}
            aria-describedby={fieldErrors.title ? `${formId}-title-error` : undefined}
            disabled={submitting}
            autoComplete="off"
          />
          {fieldErrors.title ? (
            <p id={`${formId}-title-error`} className="field__error" role="alert">
              {fieldErrors.title}
            </p>
          ) : null}
        </div>

        <div className="field">
          <label className="field__label" htmlFor={`${formId}-description`}>
            Description <span className="field__optional">(optional)</span>
          </label>
          <textarea
            id={`${formId}-description`}
            name="description"
            rows={5}
            value={description}
            maxLength={DESCRIPTION_MAX}
            onChange={(event) => setDescription(event.target.value)}
            aria-invalid={Boolean(fieldErrors.description)}
            aria-describedby={fieldErrors.description ? `${formId}-description-error` : undefined}
            disabled={submitting}
          />
          {fieldErrors.description ? (
            <p id={`${formId}-description-error`} className="field__error" role="alert">
              {fieldErrors.description}
            </p>
          ) : null}
        </div>

        <div className="ticket-form__row">
          <div className="field">
            <label className="field__label" htmlFor={`${formId}-priority`}>
              Priority <span className="field__optional">(optional — defaults to MEDIUM)</span>
            </label>
            <select
              id={`${formId}-priority`}
              name="priority"
              value={priority}
              onChange={(event) => setPriority(event.target.value as Priority | "")}
              aria-invalid={Boolean(fieldErrors.priority)}
              aria-describedby={fieldErrors.priority ? `${formId}-priority-error` : undefined}
              disabled={submitting}
            >
              <option value="">Use default (MEDIUM)</option>
              {PRIORITIES.map((value) => (
                <option key={value} value={value}>
                  {value}
                </option>
              ))}
            </select>
            {fieldErrors.priority ? (
              <p id={`${formId}-priority-error`} className="field__error" role="alert">
                {fieldErrors.priority}
              </p>
            ) : null}
          </div>

          <div className="field">
            <label className="field__label" htmlFor={`${formId}-assignee`}>
              Assignee <span className="field__optional">(optional)</span>
            </label>
            <input
              id={`${formId}-assignee`}
              name="assignee"
              type="text"
              value={assignee}
              maxLength={ASSIGNEE_MAX}
              onChange={(event) => setAssignee(event.target.value)}
              aria-invalid={Boolean(fieldErrors.assignee)}
              aria-describedby={fieldErrors.assignee ? `${formId}-assignee-error` : undefined}
              disabled={submitting}
              autoComplete="off"
            />
            {fieldErrors.assignee ? (
              <p id={`${formId}-assignee-error`} className="field__error" role="alert">
                {fieldErrors.assignee}
              </p>
            ) : null}
          </div>
        </div>

        <div className="ticket-form__actions">
          <button type="submit" className="button button--primary" disabled={submitting}>
            {submitting ? "Creating…" : "Create ticket"}
          </button>
      <Link
            href="/"
            className={`button button--secondary${submitting ? " button--disabled-link" : ""}`}
            aria-disabled={submitting}
            onClick={(event) => {
              if (submitting) {
                event.preventDefault();
              }
            }}
          >
            Cancel
          </Link>
        </div>
      </form>
    </section>
  );
}
