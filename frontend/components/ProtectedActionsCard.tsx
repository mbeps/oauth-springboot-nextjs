/**
 * Card UI component exposing buttons that trigger protected API calls.
 * Designed for the demo dashboard to verify authentication enforcement
 * on backend endpoints.  The parent supplies an `onAction` callback and
 * a loading flag which disables the buttons during in‑flight requests.
 *
 * @author Maruf Bepary
 */
import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";

/**
 * Props accepted by `ProtectedActionsCard`.
 */
type ProtectedActionsCardProps = {
  /**
   * Handler invoked when a button is clicked, receives the action name.
   */
  onAction: (action: string) => Promise<void>;
  /**
   * Whether an action request is currently in progress (disables buttons).
   */
  loading: boolean;
};

export function ProtectedActionsCard({
  onAction,
  loading,
}: ProtectedActionsCardProps) {
  return (
    <Card>
      <CardHeader>
        <CardTitle>Test Protected Actions</CardTitle>
        <CardDescription>
          These buttons require authentication and will show error toasts
        </CardDescription>
      </CardHeader>
      <CardContent>
        <div className="flex flex-wrap gap-3">
          <Button
            variant="outline"
            onClick={() => onAction("test_action")}
            disabled={loading}
          >
            Test Action
          </Button>
          <Button
            variant="outline"
            onClick={() => onAction("sample_operation")}
            disabled={loading}
          >
            Sample Operation
          </Button>
        </div>
      </CardContent>
    </Card>
  );
}
