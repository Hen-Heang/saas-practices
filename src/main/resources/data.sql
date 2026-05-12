-- ═══════════════════════════════════════════════════════════════════════
-- data.sql — auto-run AFTER schema.sql by Spring Boot
--
-- Seeds ~10 rows, so the /ntcBoard/list.do page isn't empty on first load.
-- Idempotent: it only seeds when the table is empty, so restarting the app
-- without dropping the table won't duplicate rows.
-- ═══════════════════════════════════════════════════════════════════════

INSERT INTO co_notice_m (notice_title, notice_cn, use_yn, data_reg_id)
SELECT title, content, 'Y', 'system'
  FROM (VALUES
        ('Welcome to the notice board',     'This is your first practice notice. Try editing it!'),
        ('Spring Boot 4 + MyBatis works',   'If you can read this, your DB + ORM are wired up correctly.'),
        ('Server-side rendering demo',      'Notice this page uses regular HTML form submit — no AJAX yet.'),
        ('PRG pattern in action',           'After insert/update/delete, the controller redirects to list.do.'),
        ('Search me',                       'Try typing "search" in the search box to find this row.'),
        ('Pagination test row 6',           'You will need 11+ rows to see page 2 appear.'),
        ('Pagination test row 7',           'Adjust recordCountPerPage in BaseVO if you want smaller pages.'),
        ('Soft delete demo',                'Delete this row — it goes to /ntcBoard/deletedList.do instead of being removed.'),
        ('Restore demo',                    'You can restore deleted rows from the Deleted List page.'),
        ('Audit columns',                   'data_reg_dt and data_chg_dt are set automatically by SQL NOW().')
       ) AS seed(title, content)
 WHERE NOT EXISTS (SELECT 1 FROM co_notice_m);
