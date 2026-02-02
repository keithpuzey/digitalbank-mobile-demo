import os
import time
from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.chrome.options import Options

class TestPerfecto(unittest.TestCase):
    def setUp(self):
    # ===== Perfecto cloud details =====
        PERFECTO_CLOUD = "demo.perfectomobile.com"
        SECURITY_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICI4YmI4YmZmZS1kMzBjLTQ2MjctYmMxMS0zNTYyMmY1ZDkyMGYifQ.eyJpYXQiOjE3NTYzNzc2NjMsImp0aSI6IjM1MjZiMzU5LWUzNTEtNGRhYS05YjMwLWVhZWY3NmNhOTFjZSIsImlzcyI6Imh0dHBzOi8vYXV0aC5wZXJmZWN0b21vYmlsZS5jb20vYXV0aC9yZWFsbXMvZGVtby1wZXJmZWN0b21vYmlsZS1jb20iLCJhdWQiOiJodHRwczovL2F1dGgucGVyZmVjdG9tb2JpbGUuY29tL2F1dGgvcmVhbG1zL2RlbW8tcGVyZmVjdG9tb2JpbGUtY29tIiwic3ViIjoiNjNhZDQ4ZDktOGNlNC00MWE3LTkzYjMtNDM1ZDQ4OWVjOWQwIiwidHlwIjoiT2ZmbGluZSIsImF6cCI6Im9mZmxpbmUtdG9rZW4tZ2VuZXJhdG9yIiwibm9uY2UiOiI5NjhhNjI0ZS0xMzliLTQ4Y2UtOGJmNy1hYzFmNmJjMjBjMWUiLCJzZXNzaW9uX3N0YXRlIjoiODRlOTQ4YjMtMjA1YS00OGU0LWI3MGItODYwYzM4OWE2YzY5Iiwic2NvcGUiOiJvcGVuaWQgb2ZmbGluZV9hY2Nlc3MiLCJzaWQiOiI4NGU5NDhiMy0yMDVhLTQ4ZTQtYjcwYi04NjBjMzg5YTZjNjkifQ.Qt6HSOLJZQhZYFn3XKrvxWX5kB3hcNTzYBgVKX6Qj2I"

        options = Options()
        options.set_capability("browserName", "Chrome")
        options.set_capability("browserVersion", "latest")
        options.set_capability("platformName", "Windows")
        options.set_capability("securityToken", SECURITY_TOKEN)
        options.set_capability("scriptName", "BlazeBank Full Workflow Test")

        self.driver = webdriver.Remote(
            command_executor=f"https://{PERFECTO_CLOUD}/nexperience/perfectomobile/wd/hub",
            options=options
        )
        self.driver.implicitly_wait(10)

    def test_blazebank_workflow(self): # Method MUST start with test_
        driver = self.driver
        try:
           # ===== 1. Navigate to login =====
           driver.get("http://dbankdemo.com/bank/login")
           time.sleep(1)

           driver.find_element(By.ID, "username").click()
           driver.find_element(By.ID, "username").clear()
           driver.find_element(By.ID, "username").send_keys("jsmith@demo.io")
           driver.find_element(By.ID, "password").click()
           driver.find_element(By.ID, "password").clear()
           driver.find_element(By.ID, "password").send_keys("Demo123!")

           driver.find_element(By.ID, "submit").click()
           time.sleep(1)

           # ===== 2. Check Checking Account =====
           driver.find_element(By.ID, "checking-menu").click()
           driver.find_element(By.ID, "view-checking-menu-item").click()
           time.sleep(1)

           # ===== 3. Check Savings Account =====
           driver.find_element(By.ID, "savings-menu").click()
           driver.find_element(By.ID, "view-savings-menu-item").click()
           time.sleep(1)

           # ===== 4. Check Users =====
           driver.find_element(By.LINK_TEXT, "User Reports").click()
           driver.find_element(By.CSS_SELECTOR, "button.btn.btn-primary.btn-sm").click()
           time.sleep(1)

           # ===== 5. Logout =====
           driver.find_element(By.CSS_SELECTOR, "img.user-avatar.rounded-circle").click()
           driver.find_element(By.LINK_TEXT, "Logout").click()

           # ===== Perfecto pass reporting =====
           driver.execute_script("mobile:status", {
               "status": "passed",
               "comment": "Full BlazeDemo workflow completed successfully"
           })

       except Exception as e:
           # ===== Perfecto fail reporting =====
           driver.execute_script("mobile:status", {
               "status": "failed",
               "comment": f"Test failed: {str(e)}"
           })
           raise e

    def tearDown(self):
        if self.driver:
            self.driver.quit()

if __name__ == "__main__":
    unittest.main()