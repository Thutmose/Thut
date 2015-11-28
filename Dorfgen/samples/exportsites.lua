local function write_gamelog(msg)
    local log = io.open('sites.txt', 'a')
    log:write(msg.."\n")
    log:close()
end

local function fullname(item)
    return dfhack.TranslateName(item.name)..' ('..dfhack.TranslateName(item.name ,true)..')'
end

function tablelength(T)
  local count = 0
  for _ in pairs(T) do count = count + 1 end
  return count
end

local args = {...}

local i = 1
local num = 0
local site = df.world_site.find(i)

local sized = false
sized = site.global_min_x ~= site.global_max_x
local message = i..''

while not df.isnull(site) do
	sized = site.global_min_x ~= site.global_max_x
	sized = true
	
	if sized then
		num = num + 1
		message = i..':'..site.global_min_x..','..site.global_min_y..'->'..site.global_max_x..','..site.global_max_y
		write_gamelog(message)
	end
	
	i = i + 1
	site = df.world_site.find(i)
end

print(num.." Sized sites")


