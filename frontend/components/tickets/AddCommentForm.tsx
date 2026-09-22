"use client";

import { useId, useRef, useState, type FormEvent } from "react";
import { InlineAlert } from "@/components/ui/InlineAlert";
import { ApiClientError } from "@/lib/api/http";
import { addComment } from "@/lib/api/tickets";
import { ErrorCodes, type CommentDto, type ErrorDetail } from "@/lib/types/api";

const BODY_MAX = 5_000;

type AddCommentFormProps = {
  ticketId: string;
  onAdded: (comment: CommentDto) => void;
  onNotFound: (message: string) => void;
};

/**
 * Add-comment form on ticket details (UIF-005 / API-005).
 */
export function AddCommentForm({ ticketId, onAdded, onNotFound }: AddCommentFormProps) {
  const formId = useId();
  const bodyRef = useRef<HTMLTextAreaElement>(null);

  const [body, setBody] = useState("");
  const [fieldError, setFieldError] = useState<string | null>(null);
  const [formError, setFormError] = useState<{ message: string; details?: ErrorDetail[] } | null>(
    null
  );
  const [submitting, setSubmitting] = useState(false);

  function validateClient(): string | null {
    const trimmed = body.trim();
    if (!trimmed) {
      return "Comment is required";
    }
    if (trimmed.length > BODY_MAX) {
      return `Comment must be at most ${BODY_MAX} characters`;
    }
    return null;
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (submitting) {
      return;
    }

    const clientError = validateClient();
    setFieldError(clientError);
    setFormError(null);
    if (clientError) {
      bodyRef.current?.focus();
      return;
    }

    setSubmitting(true);
    try {
      const created = await addComment(ticketId, { body });
      setBody("");
      setFieldError(null);
      setFormError(null);
      onAdded(created);
    } catch (err) {
      if (err instanceof ApiClientError) {
        if (err.status === 404 || err.errorDto?.code === ErrorCodes.TICKET_NOT_FOUND) {
          onNotFound(err.errorDto?.message ?? "This ticket could not be found.");
          return;
        }
        const details = err.errorDto?.details ?? [];
        const bodyDetail = details.find((detail) => detail.field === "body");
        setFieldError(bodyDetail?.message ?? null);
        setFormError({
          message: err.message,
          details: details.filter((detail) => detail.field !== "body"),
        });
      } else {
        setFormError({ message: "Unable to add comment. Please try again." });
      }
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <section className="ticket-panel add-comment" aria-labelledby={`${formId}-heading`}>
      <h2 id={`${formId}-heading`}>Add comment</h2>

      {formError ? (
        <InlineAlert
          tone="error"
          title="Could not add comment"
          message={formError.message}
          details={formError.details}
        />
      ) : null}

      <form className="ticket-form" onSubmit={handleSubmit} noValidate aria-busy={submitting}>
        <div className="field">
          <label className="field__label" htmlFor={`${formId}-body`}>
            Comment <span className="field__required">(required)</span>
          </label>
          <textarea
            ref={bodyRef}
            id={`${formId}-body`}
            name="body"
            rows={4}
            value={body}
            maxLength={BODY_MAX}
            onChange={(event) => setBody(event.target.value)}
            aria-invalid={Boolean(fieldError)}
            aria-describedby={fieldError ? `${formId}-body-error` : undefined}
            disabled={submitting}
            placeholder="Write a comment…"
          />
          {fieldError ? (
            <p id={`${formId}-body-error`} className="field__error" role="alert">
              {fieldError}
            </p>
          ) : null}
        </div>

        <div className="ticket-form__actions">
          <button type="submit" className="button button--primary" disabled={submitting}>
            {submitting ? "Adding…" : "Add comment"}
          </button>
        </div>
      </form>
    </section>
  );
}
