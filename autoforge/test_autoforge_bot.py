import os
import unittest
from unittest.mock import Mock
from autoforge_bot import is_owner

class OwnerGuardTests(unittest.TestCase):
    def setUp(self):
        os.environ["TELEGRAM_CHAT_ID"] = "12345"
        import autoforge_bot
        autoforge_bot.OWNER_CHAT_ID = "12345"

    def test_owner_allowed(self):
        update = Mock()
        update.effective_chat.id = 12345
        self.assertTrue(is_owner(update))

    def test_non_owner_denied(self):
        update = Mock()
        update.effective_chat.id = 999
        self.assertFalse(is_owner(update))

if __name__ == "__main__":
    unittest.main()
