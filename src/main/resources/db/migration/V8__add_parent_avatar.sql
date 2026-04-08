INSERT INTO avatars_catalog (id, name, category, url, sort_order)
SELECT 'parent-1', 'Padre', 'characters', 'https://cdn.classgo.app/avatars/parent-1.png', 7
WHERE NOT EXISTS (SELECT 1 FROM avatars_catalog WHERE id = 'parent-1');
