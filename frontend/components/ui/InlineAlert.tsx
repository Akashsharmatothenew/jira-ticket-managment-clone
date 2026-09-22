import type { ErrorDetail } from "@/lib/types/api";

type InlineAlertProps = {
  tone?: "error" | "info" | "success";
  title: string;
  message: string;
  details?: ErrorDetail[];
};

/**
 * Inline page alert (DEC-009f).
 */
export function InlineAlert({ tone = "error", title, message, details }: InlineAlertProps) {
  return (
    <div className={`inline-alert inline-alert--${tone}`} role="alert">
      <strong className="inline-alert__title">{title}</strong>
      <p className="inline-alert__message">{message}</p>
      {details && details.length > 0 ? (
        <ul className="inline-alert__details">
          {details.map((detail, index) => (
            <li key={`${detail.field ?? "detail"}-${index}`}>
              {detail.field ? `${detail.field}: ${detail.message}` : detail.message}
            </li>
          ))}
        </ul>
      ) : null}
    </div>
  );
}
